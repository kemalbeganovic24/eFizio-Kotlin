# eFizio (Android)

**Mobile therapy & patient management for Android**

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black)

**eFizio** je nativna Android aplikacija napisana u **Kotlinu** uz **Jetpack Compose**. Namijenjena je fizioterapeutima i praksama za zakazivanje termina, vođenje kartona pacijenata i praćenje tretmana poput **hidžame (cupping terapije)** i masaža.

Podaci se sinhronizuju sa Firebase backendom u realnom vremenu, a aplikacija podržava lokalne podsjetnike, historiju tretmana neovisnu od kalendara te svijetlu i tamnu temu.

---

## Sadržaj

- [Arhitektura](#arhitektura)
- [Funkcionalnosti](#funkcionalnosti)
- [Tehnologije](#tehnologije)
- [Struktura projekta](#struktura-projekta)
- [Podatkovni model i baza](#podatkovni-model-i-baza)
- [Lokalne notifikacije](#lokalne-notifikacije)
- [Instalacija i pokretanje](#instalacija-i-pokretanje)
- [Napomene](#napomene)
- [Autor](#autor)

---

## Arhitektura

Aplikacija koristi **MVVM** (Model-View-ViewModel) uz **Repository pattern**, čime su korisničko sučelje, poslovna logika i rad sa podacima međusobno razdvojeni.

```text
[ Jetpack Compose UI ] <--> [ ViewModel / Coroutines ] <--> [ FizioRepository ] <--> [ Firebase ]
          |
          v
[ LocalNotificationManager ] --> (AlarmManager / NotificationManager)
```

---

## Funkcionalnosti

| Funkcionalnost | Opis |
|---|---|
| **Upravljanje terminima** | Pregled, pretraga, zakazivanje i brisanje termina uz ažuriranje u realnom vremenu |
| **Trajna historija pacijenata** | Kartoni i historija tretmana čuvaju se nezavisno od rasporeda u kalendaru |
| **Lokalni podsjetnici** | Automatske notifikacije prije zakazanog termina |
| **Svijetla / tamna tema** | Dinamička podrška za obje teme |
| **Brza pretraga** | Pretraga termina po imenu pacijenta |
| **Firebase sinhronizacija** | Podaci se ažuriraju u realnom vremenu na svim uređajima |

---

## Tehnologije

| Dio | Tehnologija |
|---|---|
| Jezik | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Arhitektura | MVVM, Repository pattern |
| Asinkroni rad | Kotlin Coroutines, Flow |
| Baza | Firebase |
| Notifikacije | `AlarmManager`, `NotificationManager` |
| Minimalni OS | Android 8.0 (API 26) |

---

## Struktura projekta

```text
app/src/main/java/com/example/efizio/
├── model/
│   └── Appointment.kt                # Model podataka za termin
├── data/
│   └── FizioRepository.kt            # Firebase sinhronizacija i CRUD operacije
├── notifications/
│   └── LocalNotificationManager.kt   # Zakazivanje lokalnih podsjetnika
├── ui/
│   ├── HomeScreen.kt                 # Pregled termina i pretraga
│   ├── BookingScreen.kt              # Zakazivanje novog termina
│   └── theme/                        # Boje, tipografija, tema
├── viewmodel/
│   └── FizioViewModel.kt             # Stanje ekrana i poslovna logika
└── MainActivity.kt                   # Ulazna tačka aplikacije
```

> Nazive fajlova i paketa uskladi sa stvarnom strukturom projekta.

---

## Podatkovni model i baza

Svaki termin se čuva kao zaseban zapis u Firebase bazi. Primjer:

```json
{
  "imePrezime": "Ime Prezime",
  "datum": "2026-10-25",
  "vrijeme": "18:30",
  "vrstaTretmana": "Hidžama",
  "brojCasica": 8,
  "mjesto": "Leđa"
}
```

Historija pacijenata se čuva odvojeno od termina, pa brisanje termina iz kalendara ne briše karton pacijenta.

> Nazive polja i kolekcija uskladi sa stvarnom bazom.

---

## Lokalne notifikacije

Za svaki termin aplikacija zakazuje podsjetnike u unaprijed definisanim intervalima prije početka (npr. 30, 15, 10, 5 i 1 minut). Zakazivanje se vrši preko `AlarmManager`-a, a prikaz preko `NotificationManager`-a.

Na Androidu 13 i novijem potrebna je dozvola za prikaz notifikacija:

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
```

Za tačno zakazivanje na Androidu 12 i novijem može biti potrebna i dozvola za tačne alarme (`SCHEDULE_EXACT_ALARM`).

---

## Instalacija i pokretanje

### Zahtjevi

- Android Studio (novija verzija)
- JDK 17
- Android uređaj ili emulator (API 26+)
- Firebase projekat

### Koraci

**1. Kloniraj repozitorij**

```bash
git clone https://github.com/TVOJ-USERNAME/eFizio-Android.git
```

**2. Podesi Firebase**

1. Kreiraj projekat u [Firebase Console](https://console.firebase.google.com/).
2. Dodaj Android aplikaciju sa svojim *package name*-om.
3. Preuzmi **`google-services.json`** i stavi ga u folder `app/`.

**3. Pokreni aplikaciju**

1. Otvori projekat u Android Studiju i sačekaj Gradle sync.
2. Odaberi emulator ili uređaj i pokreni (**Run ▶**).

---

## Napomene

- **Privatnost podataka:** aplikacija radi sa podacima pacijenata. Prije stvarne upotrebe podesi **Firebase Security Rules** i prijavu korisnika (Firebase Authentication).
- **Konfiguracija:** `google-services.json` sadrži podatke tvog Firebase projekta i ne treba ga objavljivati u javnom repozitoriju. Dodaj ga u `.gitignore`.

---

## Autor

**Kemal Beganović**
