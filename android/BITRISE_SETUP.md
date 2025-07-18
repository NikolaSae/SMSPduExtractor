# Bitrise Cloud Build Setup - SMS PDU Analyzer Android App

## Šta je Bitrise?
Bitrise je cloud platforma za kontinuiranu integraciju (CI/CD) koja omogućava automatsko kompajliranje Android aplikacija u oblaku bez potrebe za lokalnim Android Studio-om.

## Korak 1: Kreiranje GitHub Repository-ja

1. **Kreirajte GitHub nalog** (ako ga nemate):
   - Idite na https://github.com
   - Registrujte se ili se prijavite

2. **Kreirajte novi repository**:
   - Kliknite na "New repository"
   - Ime: `sms-pdu-analyzer-android`
   - Označite kao "Public" (ili Private ako imate Pro nalog)
   - Kliknite "Create repository"

3. **Upload-ujte Android kod**:
   - Preuzmite ceo `android` folder sa ovog projekta
   - Otvorite GitHub repository
   - Kliknite "uploading an existing file"
   - Prevucite sve fajlove iz `android` foldera
   - Commit message: "Initial Android app upload"
   - Kliknite "Commit changes"

## Korak 2: Registracija na Bitrise

1. **Idite na Bitrise**:
   - Otvorite https://app.bitrise.io
   - Kliknite "Sign up free"

2. **Povežite GitHub**:
   - Izaberite "Sign up with GitHub"
   - Autorizujte Bitrise da pristupi vašem GitHub nalogu

3. **Dodajte aplikaciju**:
   - Kliknite "Add new app"
   - Izaberite vaš GitHub repository `sms-pdu-analyzer-android`
   - Kliknite "Next"

## Korak 3: Konfiguracija Build-a

1. **Automatska detekcija**:
   - Bitrise će automatski detektovati da je Android projekat
   - Izaberite branch: `main` (ili `master`)
   - Kliknite "Next"

2. **Validacija**:
   - Bitrise će validirati `build.gradle` fajl
   - Ako je sve u redu, kliknite "Confirm"

3. **Postavke**:
   - App name: `SMS PDU Analyzer`
   - Privacy: `Public` (ili Private)
   - Kliknite "Register app"

## Korak 4: Pokretanje Build-a

1. **Automatski build**:
   - Bitrise će automatski pokrenuti prvi build
   - Ovo može potrajati 10-15 minuta

2. **Praćenje progress-a**:
   - Idite na "Builds" tab
   - Kliknite na trenutni build da vidite log

3. **Čekanje završetka**:
   - Build je završen kada vidite zelenu kvačicu
   - Crveni X znači da je build neuspešan

## Korak 5: Preuzimanje APK fajla

1. **Kada je build završen**:
   - Kliknite na uspešan build
   - Scroll down do "Apps & Artifacts" sekcije

2. **Pronađite APK**:
   - Trebalo bi da vidite `app-debug.apk`
   - Kliknite na "Download" ili "Install"

3. **Instalacija**:
   - Ako koristite Android device, kliknite "Install"
   - Ili download-ujte APK i pošaljite ga na telefon

## Korak 6: Slanje APK-a na telefon

1. **Preko email-a**:
   - Download-ujte APK na računar
   - Pošaljite sebi email sa APK fajlom
   - Otvorite email na telefonu i download-ujte

2. **Preko cloud storage-a**:
   - Upload-ujte APK na Google Drive/Dropbox
   - Pristupite sa telefona i download-ujte

3. **Instalacija na telefonu**:
   - Omogućite "Unknown sources" u podešavanjima
   - Otvorite APK fajl
   - Kliknite "Install"

## Troubleshooting

### Build pada sa greškom:
1. **Proverite gradle fajlove**:
   - Uporedite sa radnim build.gradle fajlovima
   - Proverite da li su sve dependency-je dostupne

2. **Proverite Android SDK verziju**:
   - U `build.gradle` fajlu proverite `compileSdk` i `targetSdk`

3. **Proverite Bitrise log**:
   - Otvorite failed build
   - Čitajte error message u log-u

### APK se ne instalira:
1. **Proverite Android verziju**:
   - App zahteva Android 7.0+ (API 24)

2. **Proverite dozvole**:
   - Omogućite "Install unknown apps" za browser/file manager

## Automatski Build-ovi

Bitrise može automatski da kompajlira novu verziju kada:
- Push-ujete novi kod na GitHub
- Kreirate Pull Request
- Postavite scheduled build-ove

## Alternativni servisi

Ako Bitrise ne radi, možete koristiti:
- **GitHub Actions** (besplatno za javne repo-je)
- **CircleCI** (besplatno sa ograničenjima)
- **Travis CI** (besplatno za open source)

## Napomene

- Bitrise ima besplatnu opciju sa ograničenjima
- Build vreme je obično 10-20 minuta
- APK fajl je važeći 30 dana na Bitrise
- Za produkciju, koristite signed APK sa proper keystore-om