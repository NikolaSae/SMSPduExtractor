# SMS PDU Analyzer

## Overview

This is a full-stack web application that analyzes SMS PDU (Protocol Data Unit) data. The application allows users to input hexadecimal PDU strings and parses them to extract readable information like sender details, message content, timestamps, and technical specifications. It's built with a React frontend and Express backend, using PostgreSQL for data storage.

## User Preferences

Preferred communication style: Simple, everyday language.

## System Architecture

### Frontend Architecture
- **Framework**: React 18 with TypeScript
- **Styling**: Tailwind CSS with shadcn/ui components
- **State Management**: TanStack Query for server state management
- **Routing**: Wouter for client-side routing
- **Build Tool**: Vite for development and production builds
- **UI Components**: Radix UI primitives with custom styling

### Backend Architecture
- **Framework**: Express.js with TypeScript
- **Runtime**: Node.js with ES modules
- **Database ORM**: Drizzle ORM for type-safe database interactions
- **API Design**: RESTful API with JSON responses
- **Error Handling**: Centralized error handling middleware

### Database
- **Primary Database**: PostgreSQL (configured via Drizzle)
- **ORM**: Drizzle ORM with migrations support
- **Connection**: Neon Database serverless PostgreSQL
- **Fallback**: In-memory storage for development/testing

## Key Components

### PDU Parser Service
- **Location**: `server/services/pdu-parser.ts`
- **Purpose**: Core logic for parsing SMS PDU hex strings
- **Features**: 
  - Extracts SMSC information, sender details, message content
  - Handles different encoding types (GSM 7-bit, UCS2)
  - Provides technical breakdown of PDU structure
  - Error handling for malformed PDUs

### Database Schema
- **Table**: `pdu_analysis` - Stores parsed PDU data
- **Fields**: Raw PDU, parsed components, technical details, timestamps
- **Validation**: Zod schemas for type safety and validation

### Frontend Components
- **PduAnalyzer**: Main input component for PDU analysis
- **PduResults**: Displays parsed results with technical details
- **EducationalSection**: Provides learning content about PDU format
- **SamplePduSection**: Pre-configured sample PDUs for testing

## Data Flow

1. **Input**: User enters hexadecimal PDU string through React form
2. **Validation**: Frontend validates hex format using Zod schema
3. **API Call**: POST request to `/api/pdu/analyze` endpoint
4. **Parsing**: Backend PduParser service processes the PDU string
5. **Storage**: Parsed data stored in PostgreSQL via Drizzle ORM
6. **Response**: Parsed results returned to frontend
7. **Display**: Results shown with technical breakdown and visual formatting

## External Dependencies

### Database
- **Neon Database**: Serverless PostgreSQL hosting
- **Connection**: Via `@neondatabase/serverless` driver
- **Migrations**: Drizzle Kit for schema management

### UI Framework
- **shadcn/ui**: Pre-built accessible components
- **Radix UI**: Unstyled accessible primitives
- **Tailwind CSS**: Utility-first styling framework

### Development Tools
- **Vite**: Fast build tool with HMR
- **TypeScript**: Type safety across the stack
- **TanStack Query**: Server state management with caching

## Deployment Strategy

### Build Process
- **Frontend**: Vite builds React app to `dist/public`
- **Backend**: esbuild bundles Express server to `dist/index.js`
- **Database**: Drizzle migrations run via `db:push` command

### Environment Configuration
- **Development**: Uses Vite dev server with proxy to Express
- **Production**: Serves static files from Express with API routes
- **Database**: Requires `DATABASE_URL` environment variable

### Scripts
- `dev`: Runs development server with hot reload
- `build`: Builds both frontend and backend for production
- `start`: Runs production server
- `db:push`: Applies database schema changes

The application follows a modern full-stack architecture with type safety throughout, responsive design, and educational content to help users understand SMS PDU format. The codebase is well-organized with clear separation of concerns between frontend components, backend services, and database operations.

## Android Application

### Native Android App Features
- **Automatic PDU Extraction**: Captures SMS PDU data from incoming messages using BroadcastReceiver
- **Manual PDU Analysis**: Allows users to manually input PDU strings for analysis
- **Local Storage**: Uses SQLite database for storing PDU analyses
- **Material Design UI**: Clean, card-based interface with proper Android design patterns
- **Real-time Processing**: Background service processes PDU data immediately upon SMS receipt

### Android Architecture
- **MainActivity**: Main screen with RecyclerView showing all analyzed PDUs
- **PduAnalysisActivity**: Detailed view of individual PDU analysis
- **SmsReceiver**: BroadcastReceiver for capturing incoming SMS messages
- **PduExtractionService**: Background service for processing PDU data
- **PduParser**: Kotlin implementation of SMS PDU parsing logic (mirrors web app parser)
- **DatabaseHelper**: SQLite database operations for local storage
- **Adapters**: RecyclerView adapters for displaying PDU data

### Android Technical Stack
- **Language**: Kotlin with Java interoperability
- **UI Framework**: Android Material Design Components
- **Database**: SQLite with custom DatabaseHelper
- **Architecture**: MVVM pattern with RecyclerViews
- **Permissions**: SMS receive/read permissions for automatic extraction
- **Target SDK**: Android API 34 (minimum API 24)

### Data Flow (Android)
1. **SMS Reception**: BroadcastReceiver captures incoming SMS PDU data
2. **Background Processing**: PduExtractionService processes PDU in background
3. **Parsing**: PduParser (Kotlin) analyzes PDU structure and content
4. **Storage**: DatabaseHelper stores parsed data in local SQLite database
5. **UI Update**: MainActivity refreshes RecyclerView with new analysis
6. **Detail View**: PduAnalysisActivity shows comprehensive breakdown

### Recent Changes
- Created complete native Android application with automatic PDU extraction
- Implemented SQLite database for local storage of PDU analyses
- Added real-time SMS monitoring and processing capabilities
- Created Material Design UI with proper Android patterns
- Implemented comprehensive PDU parsing in Kotlin (mirrors web parser)
- Added permission handling for SMS access
- Created detailed technical breakdown views