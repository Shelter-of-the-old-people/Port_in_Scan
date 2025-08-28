# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Spring Boot 3.5.4 application called "Port_in_Scan"** - an AI-powered portfolio scanning and search platform with JWT authentication. The application serves as a comprehensive job portfolio management and recruitment matching service, utilizing advanced AI technologies including RAG (Retrieval-Augmented Generation) and vector similarity search.

### Service Purpose
- **Job Seekers**: Systematically manage and store employment-related information including certifications, skills, career history, desired positions, and cover letters
- **Recruiters**: Efficiently find and review qualified candidates through AI-powered search capabilities

### Target Users
- **Job Seekers**: All-field job seekers (STEM-focused but includes all job categories)
- **Recruiters**: HR personnel and hiring managers

## Technology Stack

### Core Framework
- **Framework**: Spring Boot 3.5.4
- **Language**: Java 21
- **Build Tool**: Gradle
- **Architecture**: Domain-Driven Design

### Databases
- **Primary Database**: MariaDB (main data storage)
- **Vector Database**: PostgreSQL + pgvector (semantic search)
- **Testing**: H2 (in-memory for tests)
- **Caching**: Redis

### AI/ML Integration
- **AI Provider**: OpenAI GPT API
- **Search Technology**: RAG (Retrieval-Augmented Generation)
- **Vector Operations**: pgvector for similarity search
- **OCR**: Tesseract for PDF text extraction

### Additional Technologies
- Spring Security with JWT
- Spring Data JPA
- Spring Batch
- Elasticsearch integration
- Auth0 JWT library
- Swagger/OpenAPI 3

## Commands

### Build & Run
- **Build**: `./gradlew build` (Windows: `gradlew.bat build`)
- **Run application**: `./gradlew bootRun`
- **Run tests**: `./gradlew test`
- **Run single test**: `./gradlew test --tests "ClassName.methodName"`
- **Clean build**: `./gradlew clean build`

### Development
- **Generate wrapper**: `./gradlew wrapper`
- **Check dependencies**: `./gradlew dependencies`

## Architecture

### Domain Structure
The application follows Domain-Driven Design with these main domains:

#### **Member Domain** (`domain.member`)
- User management and authentication
- Authentication handlers and custom user details
- JWT-based security with access/refresh tokens
- User entity with Role enum (JOBSEEKER, RECRUITER)
- Registration with role selection

#### **Portfolio Domain** (`domain.portfolio`)
- Comprehensive portfolio management system
- Personal information (name, contact, address)
- Education history
- Career/experience records
- Technical skills with proficiency levels
- Certifications
- Desired positions
- Cover letters
- File upload and OCR processing
- CRUD operations for all portfolio components

#### **Search Domain** (`domain.search`)
- AI-powered semantic search functionality
- Vector similarity search using pgvector
- RAG-based query processing
- Search history tracking
- Popular searches management
- Natural language query support

#### **AI Domain** (`domain.ai`)
- OpenAI API integration
- Embedding generation and management
- OCR text extraction and processing
- Privacy protection and data masking
- Context building for RAG pipeline

### Security Architecture
- Custom JWT authentication with separate access/refresh tokens
- Spring Security configuration with custom filters
- Login success/failure handlers
- Custom username/password authentication filter
- Role-based access control (JOBSEEKER vs RECRUITER)

### Database Architecture

#### MariaDB Schema (Primary Data)
```sql
-- Users table
users (
    user_id BIGINT PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    role ENUM('JOBSEEKER', 'RECRUITER'),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Jobseeker profiles
jobseeker_profiles (
    profile_id BIGINT PRIMARY KEY,
    user_id BIGINT FOREIGN KEY,
    name VARCHAR(100),
    phone VARCHAR(20),
    address VARCHAR(255),
    birth_date DATE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Education, experience, skills, certifications, cover_letters, etc.
-- (Full schema available in project documentation)
```

#### PostgreSQL + pgvector Schema (Vector Search)
```sql
-- Vector embeddings table
profile_embeddings (
    embedding_id BIGINT PRIMARY KEY,
    profile_id BIGINT,
    content_type VARCHAR(50), -- 'cover_letter', 'skills', 'experience'
    content_text TEXT,
    embedding vector(1536), -- OpenAI embedding dimension
    created_at TIMESTAMP
);

-- Vector search index
CREATE INDEX ON profile_embeddings USING ivfflat (embedding vector_cosine_ops);
```

### Configuration
- **Profiles**: `local`, `dev`, `prod`
- **Database**: MariaDB for production, H2 for testing, PostgreSQL for vectors
- **External configs**: JWT settings in separate `jwt.yaml`
- **Environment variables**:
  - `DB_URL`, `DB_ID`, `DB_PW` for MariaDB
  - `VECTOR_DB_URL`, `VECTOR_DB_USERNAME`, `VECTOR_DB_PASSWORD` for PostgreSQL
  - `JWT_SECRET_KEY` for JWT signing
  - `OPENAI_API_KEY` for AI integration

## Core Features Implementation

### 1. AI-Powered Search System

#### RAG Pipeline Service
```java
@Service
public class AISearchService {
    
    @Autowired
    private OpenAIService openAIService;
    
    @Autowired
    private VectorSearchService vectorSearchService;
    
    public SearchResponse searchJobseekers(String query) {
        // 1. Query vectorization
        List<Float> queryEmbedding = openAIService.createEmbedding(query);
        
        // 2. Vector similarity search
        List<ProfileEmbedding> similarProfiles = 
            vectorSearchService.findSimilar(queryEmbedding, 10);
        
        // 3. Context building
        String context = buildContext(similarProfiles);
        
        // 4. GPT response generation
        String response = openAIService.generateResponse(query, context);
        
        return new SearchResponse(response, similarProfiles);
    }
}
```

#### Embedding Generation and Storage
```java
@Service
public class EmbeddingService {
    
    public void createProfileEmbeddings(Long profileId) {
        JobseekerProfile profile = profileRepository.findById(profileId);
        
        // Cover letter embeddings (Priority 1)
        List<CoverLetter> coverLetters = coverLetterRepository.findByProfileId(profileId);
        for (CoverLetter letter : coverLetters) {
            List<Float> embedding = openAIService.createEmbedding(letter.getContent());
            saveEmbedding(profileId, "cover_letter", letter.getContent(), embedding);
        }
        
        // Skills embeddings (Priority 2)
        String skillsText = buildSkillsText(profile.getSkills());
        List<Float> skillsEmbedding = openAIService.createEmbedding(skillsText);
        saveEmbedding(profileId, "skills", skillsText, skillsEmbedding);
        
        // Experience embeddings (Priority 3)
        String experienceText = buildExperienceText(profile.getExperiences());
        List<Float> expEmbedding = openAIService.createEmbedding(experienceText);
        saveEmbedding(profileId, "experience", experienceText, expEmbedding);
    }
}
```

### 2. OCR Integration

#### PDF Text Extraction Service
```java
@Service
public class OCRService {
    
    @Autowired
    private TesseractService tesseractService;
    
    public String extractTextFromPDF(MultipartFile file) {
        try {
            PDDocument document = PDDocument.load(file.getInputStream());
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            
            StringBuilder extractedText = new StringBuilder();
            
            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300);
                String pageText = tesseractService.extractText(image);
                extractedText.append(pageText).append("\n");
            }
            
            document.close();
            return extractedText.toString();
            
        } catch (Exception e) {
            throw new OCRProcessingException("PDF text extraction failed", e);
        }
    }
    
    public JobseekerProfile parseExtractedText(String text) {
        String prompt = "Structure the following resume text into JSON format: " + text;
        String structuredData = openAIService.generateResponse(prompt, "");
        return jsonToProfile(structuredData);
    }
}
```

### 3. Privacy Protection Service

#### Personal Information Masking
```java
@Service
public class PrivacyService {
    
    public JobseekerProfileDTO maskPersonalInfo(JobseekerProfile profile) {
        JobseekerProfileDTO dto = new JobseekerProfileDTO();
        
        // Name masking (surname only)
        dto.setName(maskName(profile.getName()));
        
        // Address masking (approximate location only)
        dto.setAddress(maskAddress(profile.getAddress()));
        
        // Public information
        dto.setSkills(profile.getSkills());
        dto.setExperiences(profile.getExperiences());
        dto.setCertifications(profile.getCertifications());
        
        return dto;
    }
    
    private String maskName(String fullName) {
        if (fullName == null || fullName.length() < 2) return "Anonymous";
        return fullName.substring(0, 1) + "*".repeat(fullName.length() - 1);
    }
    
    private String maskAddress(String address) {
        // "Seoul Gangnam-gu Teheran-ro" -> "Seoul Gangnam-gu"
        String[] parts = address.split(" ");
        if (parts.length >= 2) {
            return parts[0] + " " + parts[1];
        }
        return parts[0];
    }
}
```

## API Architecture

### Authentication APIs
```
POST /api/auth/register - User registration
POST /api/auth/login - Login
POST /api/auth/refresh - Token refresh
POST /api/auth/logout - Logout
```

### Job Seeker APIs
```
GET /api/jobseeker/profile - Profile inquiry
PUT /api/jobseeker/profile - Profile update
POST /api/jobseeker/education - Education registration
POST /api/jobseeker/experience - Experience registration
POST /api/jobseeker/skills - Skills registration
POST /api/jobseeker/certifications - Certification registration
POST /api/jobseeker/cover-letters - Cover letter registration
POST /api/jobseeker/files/upload - File upload with OCR
```

### Recruiter APIs
```
POST /api/recruiter/search - AI-powered candidate search
GET /api/recruiter/jobseekers/{id} - Candidate detailed information
POST /api/recruiter/contact/{jobseeker_id} - Contact information request
```

## Key Technologies Configuration

### JWT Settings
- JWT tokens expire: Access token (30 min), Refresh token (2 weeks)
- Secret key configured via environment variable
- Role-based access control implemented

### AI Integration Settings
```yaml
openai:
  api-key: ${OPENAI_API_KEY}
  model: gpt-4
  embedding-model: text-embedding-ada-002
  max-tokens: 2000
  temperature: 0.7
```

### Vector Database Settings
```yaml
vector:
  database:
    url: ${VECTOR_DB_URL}
    username: ${VECTOR_DB_USERNAME}
    password: ${VECTOR_DB_PASSWORD}
  embedding:
    dimension: 1536
    similarity-threshold: 0.8
```

### File Upload Configuration
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
file:
  upload:
    directory: ${FILE_UPLOAD_DIR:/tmp/uploads}
    allowed-types: pdf,jpg,jpeg,png
```

## Testing Strategy

### Testing Structure
- Uses JUnit 5 platform
- Spring Boot Test starter included
- Spring Security Test support
- Spring Batch Test utilities
- Testcontainers for database integration tests
- Mockito for AI service mocking

### AI Service Testing
```java
@ExtendWith(MockitoExtension.class)
class AISearchServiceTest {
    
    @Mock
    private OpenAIService openAIService;
    
    @Mock
    private VectorSearchService vectorSearchService;
    
    @Test
    void testSearchJobseekers() {
        // Test AI search functionality
        when(openAIService.createEmbedding(anyString()))
            .thenReturn(Arrays.asList(0.1f, 0.2f, 0.3f));
        
        SearchResponse response = aiSearchService.searchJobseekers("Java developer");
        
        assertThat(response).isNotNull();
        assertThat(response.getProfiles()).isNotEmpty();
    }
}
```

## Development Phases

### Phase 1: Core Infrastructure (4 weeks)
- User authentication/authorization system
- Basic CRUD APIs
- Database design and implementation
- JWT security setup

### Phase 2: AI Search Implementation (3 weeks)
- OpenAI API integration
- RAG system implementation
- Vector database setup
- Embedding generation pipeline

### Phase 3: OCR and File Processing (2 weeks)
- PDF upload and OCR processing
- File storage and management system
- Auto-extraction of profile information

### Phase 4: Security and Optimization (1 week)
- Privacy protection features
- Performance optimization
- Testing and deployment

## Performance Specifications

### Expected Scale
- **Target Users**: ~20 users
- **Concurrent Users**: 5-10 users
- **Response Time**: API < 2sec, AI Search < 5sec
- **Availability**: 99%+

### AI Performance
- **Embedding Generation**: ~500ms per document
- **Vector Search**: <100ms for similarity queries
- **RAG Response**: 2-5 seconds depending on context size

## Important Notes

### Development Environment
- Database connection requires environment variables to be set
- OpenAI API key required for AI features
- Vector database must be properly configured with pgvector extension
- OCR requires Tesseract installation

### API Documentation
- API documentation available via Swagger UI when running
- Access at: `http://localhost:8080/swagger-ui.html`

### Security Considerations
- Personal information is masked in search results
- JWT tokens have appropriate expiration times
- Role-based access control strictly enforced
- File uploads are virus-scanned and type-validated

### AI Integration Notes
- All AI calls include proper error handling and fallbacks
- Embedding generation is asynchronous to avoid blocking
- Context size is optimized for GPT token limits
- Search results are cached to improve performance

This application represents a comprehensive AI-powered recruitment platform that combines traditional CRUD operations with cutting-edge machine learning capabilities for semantic search and intelligent matching.