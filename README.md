# Kalkulator (z ukrytym menu)

Aplikacja Android wyglądająca jak zwykły kalkulator. Wpisz `10101357` i naciśnij `=`,
aby otworzyć ukryte menu: włącznik wibracji, suwak mocy (1–100%) i tryby (stały / pulsacja / fala).

- Wyjście z menu (Home, Wstecz, wygaszenie ekranu) zawsze wyłącza wibrację.
- Menu blokuje zrzuty ekranu i podgląd w ostatnich aplikacjach.
- Kod zmienisz w `CODE` w `CalculatorActivity.java`.

## APK
Buduje go GitHub Actions (`.github/workflows/build-apk.yml`). Gotowy plik jest w Releases jako `Kalkulator.apk`
(trzeba zezwolić na instalację z nieznanych źródeł). Lokalnie: `gradle :app:assembleRelease`.
