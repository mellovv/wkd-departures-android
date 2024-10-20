package dev.mellovv.wkd_departures.data

val trainStations = mapOf(
    1 to "Grodzisk Mazowiecki Radońska",
    2 to "Grodzisk Mazowiecki Jordanowice",
    3 to "Grodzisk Mazowiecki Piaskowa",
    4 to "Grodzisk Mazowiecki Okrężna",
    5 to "Brzózki",
    6 to "Kazimierówka",
    7 to "Podkowa Leśna Zachodnia",
    8 to "Podkowa Leśna Główna",
    9 to "Podkowa Leśna Wschodnia",
    10 to "Otrębusy",
    11 to "Kanie Helenowskie",
    12 to "Nowa Wieś Warszawska",
    13 to "Komorów",
    14 to "Pruszków WKD",
    15 to "Tworki",
    16 to "Malichy",
    17 to "Reguły",
    18 to "Michałowice",
    19 to "Opacz",
    20 to "Warszawa Salomea",
    21 to "Warszawa Raków",
    22 to "Warszawa Aleje Jerozolimskie",
    23 to "Warszawa Reduta Ordona",
    24 to "Warszawa Zachodnia WKD",
    25 to "Warszawa Ochota WKD",
    26 to "Warszawa Śródmieście WKD",
    27 to "Milanówek Grudów",
    28 to "Polesie"
)

fun trainStationIdToName(stationId: Int): String {
    return trainStations[stationId] ?: "Unknown Station"
}
