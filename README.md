SMS PDU Analyzer
A comprehensive SMS PDU (Protocol Data Unit) analysis tool with both web interface and native Android application for extracting, parsing, and analyzing SMS message data.

🚀 Features
Web Application
Interactive PDU Analysis: Paste PDU strings and get instant detailed breakdown
Sample PDU Library: Pre-loaded sample PDUs for testing and learning
Educational Content: Comprehensive guide about SMS PDU format and structure
Real-time Parsing: Immediate results with technical details and explanations
Responsive Design: Works on desktop, tablet, and mobile devices
Android Application
Automatic PDU Extraction: Captures SMS PDU data from incoming messages in real-time
Manual PDU Analysis: Input PDU strings manually for analysis
Comprehensive Parsing: Extracts sender information, message content, timestamps, and technical details
Local Storage: SQLite database stores all PDU analyses locally
Material Design UI: Clean, modern interface with card-based layout
🏗️ Architecture
Web Application Stack
Frontend: React with TypeScript, Tailwind CSS, shadcn/ui components
Backend: Express.js with TypeScript
Build Tool: Vite for fast development and optimized builds
Database: In-memory storage with persistence options
Android Application
Platform: Native Android (API 24+)
Language: Kotlin
Database: SQLite with custom helper
Architecture: Service-based background processing
Permissions: SMS access for automatic PDU extraction
📱 Supported PDU Types
SMS-DELIVER: Incoming SMS messages
SMS-SUBMIT: Outgoing SMS messages
SMS-STATUS-REPORT: Delivery confirmation reports
🔧 Encoding Support
GSM 7-bit: Standard SMS encoding with full character set support
UCS2: Unicode encoding for international characters
GSM 8-bit: Binary data encoding
Hex decoding: Raw hexadecimal message parsing
🚦 Getting Started
Web Application
Development: The web app runs on Replit with automatic deployment
Access: Navigate to the deployed URL to start analyzing PDU strings
Usage:
Paste your PDU string in the input field
Click "Analyze PDU" for instant results
Explore sample PDUs to understand different message types
Android Application
Requirements:

Android 7.0 (API 24) or higher
SMS permissions for automatic extraction
Installation:

cd android
./gradlew assembleDebug
Permissions: Grant SMS permissions when prompted for automatic PDU capture

Usage:

Launch "SMS PDU Analyzer"
Send an SMS to your device for automatic analysis
Use the + button for manual PDU input
📊 What Gets Analyzed
Message Information
Sender Details: Phone number and address type
Message Content: Decoded text with proper encoding
Timestamps: Message send/receive times
Status: Message delivery status
Technical Details
SMSC Information: Service center details
PDU Type: Message classification
Encoding Scheme: Character encoding method
Protocol Identifiers: Technical message parameters
Raw Data Breakdown: Hexadecimal field analysis
🔍 Example PDU Analysis
PDU: 0791448720003023240DD0E474747A8C07C9D2E03D4BF7399FE6
Analysis:
- Type: SMS-DELIVER (Incoming)
- Sender: +1234567890
- Message: "Hello World"
- Encoding: GSM 7-bit
- Timestamp: 2024-01-15 14:30:25
🛠️ Development
Web Application Development
npm install
npm run dev
Android Development
Open android/ folder in Android Studio
Sync Gradle files
Build and run on device/emulator
📋 API Endpoints
Web API
GET /api/pdu/samples - Get sample PDU strings
POST /api/pdu/analyze - Analyze PDU string
GET /api/pdu/parse/:pdu - Parse specific PDU
🔒 Permissions (Android)
RECEIVE_SMS: Capture incoming SMS messages
READ_SMS: Read SMS content for analysis
INTERNET: Future web service integration
🐛 Troubleshooting
Web Application
Ensure PDU string is valid hexadecimal
Check for proper PDU format (minimum length requirements)
Verify network connectivity for API calls
Android Application
Build Issues: Check Android SDK version and Gradle sync
App Crashes: Ensure Android 7.0+ and proper permissions
No SMS Capture: Verify SMS permissions and try manual analysis
Encoding Issues: Check GSM 7-bit character support
📝 Testing
Sample PDU Strings
# SMS-DELIVER Example
0791448720003023240DD0E474747A8C07C9D2E03D4BF7399FE6
# SMS-SUBMIT Example  
0011000B919471476965870000080048656C6C6F
# UCS2 Encoded
079144872000302324...
🤝 Contributing
Fork the repository
Create feature branch (git checkout -b feature/amazing-feature)
Commit changes (git commit -m 'Add amazing feature')
Push to branch (git push origin feature/amazing-feature)
Open Pull Request
📜 License
This project is open source and available under the MIT License.

🔗 Links
Live Demo: SMS PDU Analyzer
Documentation: See /docs folder for detailed API documentation
Issues: Report bugs and request features in GitHub Issues
🏷️ Tags
sms pdu analysis android web-app typescript kotlin telecommunications parsing messaging
