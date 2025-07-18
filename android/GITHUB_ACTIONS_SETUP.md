# GitHub Actions Build Setup - SMS PDU Analyzer Android App

## Problem sa Bitrise
Bitrise ima problem sa gradle wrapper-om jer gradle-wrapper.jar mora biti pravi binarni fajl, a ne tekstualni placeholder. GitHub Actions je pouzdanija opcija.

## GitHub Actions - Brže i Jednostavnije

### Korak 1: Kreiranje GitHub Repository-ja

1. **Idite na GitHub**:
   - https://github.com
   - Prijavite se ili registrujte

2. **Kreirajte novi repository**:
   - Kliknite "New repository"
   - Ime: `sms-pdu-analyzer-android`
   - Označite kao "Public"
   - Kliknite "Create repository"

### Korak 2: Upload Android Projekta

1. **Preuzmite Android folder**:
   - Sačuvajte ceo `android` folder sa ovog projekta

2. **Upload na GitHub**:
   - U vašem GitHub repo kliknite "uploading an existing file"
   - Prevucite SVE fajlove iz `android` foldera
   - **VAŽNO**: Uključite i `.github` folder sa workflow fajlom
   - Commit message: "Initial Android app with GitHub Actions"
   - Kliknite "Commit changes"

### Korak 3: Pokretanje Automatskog Build-a

1. **Actions tab**:
   - Kliknite na "Actions" tab u vašem GitHub repo
   - Videćete "Build Android APK" workflow

2. **Automatski trigger**:
   - Build se automatski pokreće kada upload-ujete kod
   - Ili kliknite "Run workflow" da ga pokrenete ručno

3. **Praćenje build-a**:
   - Kliknite na running build da vidite progress
   - Build traje oko 5-10 minuta

### Korak 4: Preuzimanje APK-ja

1. **Kada je build završen**:
   - Idite na "Actions" tab
   - Kliknite na uspešan build (zelena kvačica)

2. **Artifacts sekcija**:
   - Scroll down do "Artifacts"
   - Kliknite na "app-debug" da download-ujete APK

3. **Unzip fajl**:
   - Download-ovani fajl je ZIP
   - Otvorite ga i pronađite `app-debug.apk`

### Korak 5: Instalacija na Telefon

1. **Pošaljite APK na telefon**:
   - Preko email-a, Google Drive, ili USB
   - Ili scan QR kod sa telefona da download-ujete direktno

2. **Omogućite instalaciju**:
   - Podešavanja → Bezbednost → Nepoznati izvori
   - Ili Podešavanja → Aplikacije → Poseban pristup → Instaliranje nepoznatih app-ova

3. **Instalirajte**:
   - Otvorite APK fajl na telefonu
   - Kliknite "Install"
   - Potvrdite instalaciju

### Korak 6: Testiranje App-a

1. **Prva pokretanja**:
   - Otvorite "SMS PDU Analyzer"
   - Dodelite SMS dozvole kada se traži

2. **Testiranje**:
   - Pošaljite SMS na vaš telefon da testirate automatsko hvatanje
   - Ili koristite + dugme za ručno unošenje PDU

3. **Sample PDU stringovi**:
   ```
   0791448720003023240DD0E474747A8C07C9D2E03D4BF7399FE6
   0011000B919471476965870000080048656C6C6F20576F726C6421
   ```

## Prednosti GitHub Actions

✅ **Besplatno** - 2000 build minuta mesečno za javne repo-je
✅ **Brže** - Build za 5-10 minuta umesto 20+
✅ **Pouzdanije** - Manje problema sa dependency-jima
✅ **Jednostavnije** - Manje konfiguracije potrebno
✅ **Automatski** - Build se pokreće pri svakom push-u

## Troubleshooting

### Build pada:
1. **Proverite da li je .github folder upload-ovan**
2. **Proverite Actions tab da li je workflow prikazan**
3. **Kliknite na failed build da vidite error log**

### APK se ne download-uje:
1. **Proverite da li je build uspešan (zelena kvačica)**
2. **Scroll down do Artifacts sekcije**
3. **Možda je potrebno da se ponovo prijavite na GitHub**

### APK se ne instalira:
1. **Proverite verziju Android-a (potrebno 7.0+)**
2. **Omogućite "Install unknown apps" za browser/file manager**
3. **Proverite da li je APK fajl potpuno download-ovan**

## Automatski Build-ovi

GitHub Actions automatski pravi novi APK kada:
- Push-ujete novi kod
- Neko pravi Pull Request
- Možete postaviti scheduled build-ove

## Alternativni Pristup - Lokalni Build

Ako ni GitHub Actions ne radi:
1. **Instalirajte Android Studio**
2. **Otvorite `android` folder**
3. **Build → Build Bundle(s) / APK(s) → Build APK(s)**
4. **APK će biti u: `app/build/outputs/apk/debug/app-debug.apk`**

GitHub Actions je trenutno najbolja opcija za dobijanje APK-a bez instaliranja Android Studio lokalno!