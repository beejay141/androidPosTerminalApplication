# A930 POS Lite

Android point-of-sale application for PAX payment terminals with EMV card acceptance, host connectivity, terminal parameter management, transaction history, receipts, and value-added service flows.

This repository is structured as an Android app plus several local POS/host/device integration modules and bundled vendor AARs. The codebase shows direct integration with PAX terminal services, EMV transaction orchestration, AID/CAPK loading, online authorization handling, and ISO-style host communication.

## What This Project Does

- Runs on Android-based POS hardware with PAX-specific device permissions and libraries.
- Supports card-present transaction flows such as purchase, balance inquiry, refund, reversal, transfer, and VAS-linked purchase flows.
- Downloads and stores terminal configuration and key material before allowing transactions.
- Persists transaction and configuration data locally using Room.
- Connects to multiple host backends, including GTMS, POSVAS, and TAMS.
- Includes receipt/journal generation, reporting, QR-related flows, and VAS features.

## EMV SDK and Android POS Terminal Implementation Details

The repository contains direct, reviewable implementation details showing that this is an Android POS application with EMV SDK integration:

| Implementation area               | File(s)                                                                                                                                                                                                                                                          | What it shows                                                                                                                                                                     |
| --------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Android POS app packaging         | `app/build.gradle`                                                                                                                                                                                                                                               | Android application module targeting API 27, multidex enabled, and local POS integration modules included as Gradle dependencies.                                                 |
| PAX terminal permissions          | `app/src/main/AndroidManifest.xml`                                                                                                                                                                                                                               | Declares `com.pax.permission.MAGCARD`, `PED`, `ICC`, `PICC`, and `PRINTER`, indicating magnetic stripe, PIN entry, chip, contactless, and printer device access on PAX terminals. |
| Native EMV kernel loading         | `app/src/main/java/com/iisysgroup/androidlite/App.kt`                                                                                                                                                                                                            | Loads `F_EMV_LIB_PayDroid`, `JNI_EMV_v100`, and related card-kernel libraries for entry, Mastercard, Wave, Amex, DPAS, JCB, and PURE.                                             |
| AID and CAPK initialization       | `app/src/main/java/com/iisysgroup/androidlite/App.kt`                                                                                                                                                                                                            | Calls `FileParse.parseAidFromAssets(this, "aid.ini")` and `FileParse.parseCapkFromAssets(this, "capk.ini")` during application startup.                                           |
| EMV parameter assets              | `app/src/main/assets/aid.ini`, `app/src/main/assets/capk.ini`, `app/src/main/assets/emv_param.emv`, `app/src/main/assets/clss_param.clss`                                                                                                                        | Stores EMV application identifiers, CAPKs, and related EMV/contactless parameter files bundled with the app.                                                                      |
| EMV transaction interactor usage  | `app/src/main/java/com/iisysgroup/androidlite/cardpaymentprocessors/BaseCardPaymentProcessor.kt`, `app/src/main/java/com/iisysgroup/androidlite/payments_menu/handlers/BaseHandler.kt`                                                                           | Core transaction processors are built around `EmvInteractor`, the abstraction used to start and control EMV transactions.                                                         |
| Purchase EMV flow                 | `app/src/main/java/com/iisysgroup/androidlite/payments_menu/handlers/Purchase.kt`, `app/src/main/java/com/iisysgroup/androidlite/cardpaymentprocessors/PurchaseProcessor.kt`                                                                                     | Starts `EmvTransactionType.EMV_PURCHASE`, subscribes to EMV status, sends online auth response back into the EMV flow, and stores transaction results.                            |
| Balance inquiry EMV flow          | `app/src/main/java/com/iisysgroup/androidlite/payments_menu/handlers/Balance.kt`                                                                                                                                                                                 | Starts `EmvTransactionType.EMV_INQUIRY` for card-based account inquiry.                                                                                                           |
| Refund and reversal EMV flows     | `app/src/main/java/com/iisysgroup/androidlite/payments_menu/handlers/Refund.kt`, `app/src/main/java/com/iisysgroup/androidlite/payments_menu/handlers/Revert.kt`, `app/src/main/java/com/iisysgroup/androidlite/cardpaymentprocessors/ReversalCommunicator.java` | Handles refund/reversal transaction types and serializes ICC/EMV data into host messages, including ISO field 55.                                                                 |
| Track 2 / ICC / PIN handling      | `app/src/main/java/com/iisysgroup/androidlite/cardpaymentprocessors/ReversalCommunicator.java`                                                                                                                                                                   | Extracts EMV card data such as track 2, ICC data, PAN sequence number, and PIN block information for host communication.                                                          |
| Terminal preparation workflow     | `app/src/main/java/com/iisysgroup/androidlite/TermMagmActivity.kt`                                                                                                                                                                                               | Performs terminal configuration, downloads parameters from the host, saves config to the local database, and marks terminal prep state.                                           |
| PAX device bootstrap              | `app/src/main/java/com/iisysgroup/androidlite/App.kt`, `app/src/main/java/com/iisysgroup/androidlite/cardpaymentprocessors/BaseCardPaymentProcessor.kt`                                                                                                          | Registers the PAX device and initializes the card-payment processor path around a POS device abstraction.                                                                         |
| POS/host/device libraries in repo | `settings.gradle`, `poslib-commons/`, `poslib-device/`, `poslib-host-debug/`, `PaxModule/`, `payviceconnect-debug/`                                                                                                                                              | Local modules and vendor packages required for terminal communication, host messaging, and PAX integration are present in the workspace.                                          |

For a technical review, the strongest first-pass files are:

- `app/src/main/java/com/iisysgroup/androidlite/App.kt`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/iisysgroup/androidlite/cardpaymentprocessors/PurchaseProcessor.kt`
- `app/src/main/java/com/iisysgroup/androidlite/payments_menu/handlers/Purchase.kt`
- `app/src/main/java/com/iisysgroup/androidlite/cardpaymentprocessors/ReversalCommunicator.java`
- `app/src/main/java/com/iisysgroup/androidlite/TermMagmActivity.kt`
- `app/src/main/assets/aid.ini`
- `app/src/main/assets/capk.ini`

## Technology Stack

- Android application module built with Gradle
- Kotlin 1.2.x and Java
- Android Support libraries (pre-AndroidX)
- Room persistence
- RxJava / RxAndroid
- Retrofit + Gson
- Joda Time
- PAX device and PayDroid-related native libraries
- Local proprietary POS libraries delivered as Gradle modules and AARs

## Module Overview

### Application module

- `app/`: Main Android app, activities, transaction handlers, payment processors, assets, layouts, and app bootstrap.

### Local integration modules

- `poslib-commons/`: Shared POS and EMV-related classes used across the app.
- `poslib-device/`: Device-facing abstractions and terminal interaction helpers.
- `poslib-host-debug/`: Host communication layer and entities used for authorization/config downloads.
- `PaxModule/`: PAX integration module packaged locally.
- `payviceconnect-debug/`: Additional local integration package referenced by the app.

### Additional bundled vendor artifacts

These folders contain vendor or device artifacts present in the repository, though not all are included in the active Gradle settings for this app build:

- `newland/`
- `newland-debug-old/`
- `SDK8Series/`
- `SkModule/`
- `ampsource-debug.aar`

## Transaction Flow Summary

### Card purchase

1. A payment activity launches a card payment processor.
2. The processor configures terminal/host context and starts card detection.
3. Once a card is detected, it starts an EMV transaction using `EmvInteractor`.
4. The handler performs online authorization through the configured host.
5. The host response is passed back into the EMV kernel with issuer authentication and issuer scripts.
6. The transaction is saved locally and can be printed or reviewed later.

### Other supported EMV-linked flows

- Balance inquiry
- Refund
- Reversal / revert
- Transfer
- VAS purchase coupled with card payment

## Host and Terminal Management

The app supports multiple host backends selected at runtime:

- GTMS
- POSVAS
- TAMS

Terminal preparation is a first-class workflow in this codebase:

- Validates that required network and terminal settings exist.
- Retrieves terminal keys and VAS terminal details.
- Downloads host configuration parameters.
- Stores configuration locally in the POS database.
- Marks the terminal as prepared before payment flows proceed.

## Data and Persistence

The app initializes and uses local Room databases for:

- POS library data (`poslib.db`)
- Beneficiary data (`beneficiaries.db`)
- Configuration data
- Transaction records
- Key holder / terminal state data

## Build Requirements

Because this repository depends on proprietary local modules and vendor binaries, a successful build requires the original development environment or equivalent replacements.

Minimum practical requirements:

- macOS, Linux, or Windows with Android build tooling
- JDK compatible with Android Gradle Plugin 3.2.0
- Android SDK for compileSdkVersion 27 / targetSdkVersion 27
- Gradle wrapper included in repo
- Access to local AAR/module dependencies already included in this workspace
- Any machine-specific files expected by local configuration

## Build and Run

Typical commands from the repository root:

```bash
./gradlew assembleDebug
./gradlew installDebug
```

If the wrapper script is not executable:

```bash
chmod +x gradlew
./gradlew assembleDebug
```

## Important Environment Notes

- This project is based on older Android Gradle and support-library versions.
- The app uses pre-AndroidX dependencies.
- Several dependencies are proprietary or vendor-specific and may not be available from public repositories.
- Some configuration files in the repo indicate a legacy/internal deployment environment.
- Terminal signing, service credentials, and distribution configuration should be reviewed and handled carefully before external sharing or modernization.

## Repository Layout

```text
app/                    Main Android POS application
poslib-commons/         Shared POS/EMV support module
poslib-device/          Device interaction module
poslib-host-debug/      Host communication module
PaxModule/              PAX integration module
payviceconnect-debug/   Additional local integration module
newland/                Alternate vendor artifact/module
SDK8Series/             Additional SDK artifact/module
gradle/                 Gradle wrapper support
```

## What a Technical Reviewer Can Confirm From This Repository

An EMV SDK reviewer looking for Android POS-terminal development details would typically want to confirm the following:

- Device-level terminal permissions
- Native EMV kernel bindings
- AID/CAPK management
- EMV transaction start and continuation points
- Online authorization response handoff back into the kernel
- ICC / field 55 handling
- Terminal parameter preparation and download flows
- Host communication integrated with payment flows

This repository contains all of those elements in directly inspectable source files and bundled terminal assets.

## Recommended Review Path

For a fast technical due-diligence pass, inspect these files in order:

1. `app/src/main/java/com/iisysgroup/androidlite/App.kt`
2. `app/src/main/AndroidManifest.xml`
3. `app/src/main/java/com/iisysgroup/androidlite/cardpaymentprocessors/BaseCardPaymentProcessor.kt`
4. `app/src/main/java/com/iisysgroup/androidlite/cardpaymentprocessors/PurchaseProcessor.kt`
5. `app/src/main/java/com/iisysgroup/androidlite/payments_menu/handlers/Purchase.kt`
6. `app/src/main/java/com/iisysgroup/androidlite/payments_menu/handlers/Balance.kt`
7. `app/src/main/java/com/iisysgroup/androidlite/payments_menu/handlers/Refund.kt`
8. `app/src/main/java/com/iisysgroup/androidlite/cardpaymentprocessors/ReversalCommunicator.java`
9. `app/src/main/java/com/iisysgroup/androidlite/TermMagmActivity.kt`
10. `app/src/main/assets/aid.ini`
11. `app/src/main/assets/capk.ini`
12. `settings.gradle`
