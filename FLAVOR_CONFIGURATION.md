# Branding configuration

The app now supports both **flavors** and **environment/Gradle properties** for branding.

## Flavors

- `prodigi` (default behavior)
- `custom` (for white-label branding)

Examples:

```bash
./gradlew :app:assembleProdigiDebug
./gradlew :app:assembleCustomDebug
```

## Configurable values

These can be passed either as Gradle properties (`-PKEY=value` or in `~/.gradle/gradle.properties`) or environment variables.

### Global keys (apply to default config)

- `APP_APPLICATION_ID`
- `APP_NAME`
- `APP_ICON`
- `APP_ROUND_ICON`
- `BRAND_PRIMARY_LIGHT`
- `BRAND_PRIMARY_DARK`
- `BRAND_SECONDARY_LIGHT`
- `BRAND_SECONDARY_DARK`
- `BRAND_TERTIARY_LIGHT`
- `BRAND_TERTIARY_DARK`
- `BRAND_SURFACE_LIGHT`
- `BRAND_SURFACE_DARK`
- `BRAND_ENABLE_DYNAMIC_COLOR` (`true`/`false`, defaults to `false`)

### Custom flavor override keys

- `CUSTOM_APPLICATION_ID`
- `CUSTOM_APP_NAME`
- `CUSTOM_APP_ICON`
- `CUSTOM_APP_ROUND_ICON`
- `CUSTOM_BRAND_PRIMARY_LIGHT`
- `CUSTOM_BRAND_PRIMARY_DARK`
- `CUSTOM_BRAND_SECONDARY_LIGHT`
- `CUSTOM_BRAND_SECONDARY_DARK`
- `CUSTOM_BRAND_TERTIARY_LIGHT`
- `CUSTOM_BRAND_TERTIARY_DARK`
- `CUSTOM_BRAND_SURFACE_LIGHT`
- `CUSTOM_BRAND_SURFACE_DARK`
- `CUSTOM_BRAND_ENABLE_DYNAMIC_COLOR` (`true`/`false`)

## Asset configuration

`APP_ICON` and `APP_ROUND_ICON` (and custom equivalents) should be a **mipmap resource name** (without `@mipmap/`).

Example:

```bash
./gradlew :app:assembleCustomRelease \
  -PCUSTOM_APPLICATION_ID=com.example.bookreader \
  -PCUSTOM_APP_NAME="My Reader" \
  -PCUSTOM_APP_ICON=ic_launcher_alt \
  -PCUSTOM_APP_ROUND_ICON=ic_launcher_alt_round \
  -PCUSTOM_BRAND_PRIMARY_LIGHT=#6A5AE0 \
  -PCUSTOM_BRAND_PRIMARY_DARK=#4537A8
```
