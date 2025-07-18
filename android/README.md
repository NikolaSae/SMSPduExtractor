# SMS PDU Analyzer Android App

A native Android application that automatically extracts and analyzes SMS PDU data from incoming messages.

## Features

- **Automatic PDU Extraction**: Captures SMS PDU data from incoming messages in real-time
- **Manual PDU Analysis**: Allows users to manually input PDU strings for analysis
- **Comprehensive Parsing**: Extracts sender information, message content, timestamps, and technical details
- **Local Storage**: Stores all PDU analyses locally using SQLite database
- **Technical Breakdown**: Provides detailed technical information about each PDU field
- **User-Friendly Interface**: Clean, Material Design interface with card-based layout

## Requirements

- Android API 24 (Android 7.0) or higher
- SMS permissions for automatic PDU extraction
- Internet permission (for future web service integration)

## Installation

1. Clone the repository
2. Open the `android` folder in Android Studio
3. Build and run the application
4. Grant SMS permissions when prompted

## How It Works

### Automatic PDU Extraction
1. The app registers a `BroadcastReceiver` to listen for incoming SMS messages
2. When an SMS is received, the PDU data is extracted from the intent
3. The raw PDU bytes are converted to hexadecimal format
4. The PDU is parsed using a custom parser to extract all relevant information
5. The analysis is stored in the local database and displayed in the app

### Manual Analysis
1. Users can tap the floating action button to manually input PDU strings
2. The same parsing logic analyzes the manually entered PDU
3. Results are displayed with full technical breakdown

## Architecture

### Components

- **MainActivity**: Main activity displaying list of analyzed PDUs
- **PduAnalysisActivity**: Detailed view of individual PDU analysis
- **SmsReceiver**: BroadcastReceiver for capturing incoming SMS messages
- **PduExtractionService**: Service for processing PDU data in background
- **PduParser**: Core parsing logic for SMS PDU format
- **DatabaseHelper**: SQLite database operations
- **Adapters**: RecyclerView adapters for displaying data

### Data Model

- **PduAnalysis**: Main data class containing all parsed PDU information
- **TechnicalDetail**: Individual technical field details
- **PduBreakdown**: Raw PDU data breakdown

## Permissions

The app requires the following permissions:

- `RECEIVE_SMS`: To capture incoming SMS messages
- `READ_SMS`: To read SMS content
- `INTERNET`: For future web service integration

## Technical Details

### PDU Format Support

The app supports parsing of:
- SMS-DELIVER (incoming messages)
- SMS-SUBMIT (outgoing messages)
- SMS-STATUS-REPORT (delivery reports)

### Encoding Support

- GSM 7-bit encoding
- UCS2 encoding
- Basic hex message decoding

### Database Schema

The app uses SQLite to store:
- Raw PDU data
- Parsed message information
- Technical details (as JSON)
- Timestamps and metadata

## Usage

1. **Grant Permissions**: Allow SMS permissions when prompted
2. **Automatic Analysis**: Send an SMS to your device to see automatic PDU extraction
3. **Manual Analysis**: Use the + button to manually enter PDU strings
4. **View Details**: Tap on any analysis to see detailed breakdown
5. **Technical Info**: Scroll through technical details to understand PDU structure

## Development

### Building

1. Open Android Studio
2. File → Open → Select the `android` folder
3. Wait for Gradle sync to complete
4. Run the app on device or emulator

### Testing

- Use the sample PDU strings from the web application
- Test with real SMS messages on physical device
- Verify permission handling and error cases

## Future Enhancements

- Integration with web service for cloud storage
- Export functionality for PDU data
- More encoding format support
- Dark mode support
- PDU visualization improvements

## Related Projects

This Android app works alongside the SMS PDU Analyzer web application, sharing the same parsing logic and analysis capabilities.