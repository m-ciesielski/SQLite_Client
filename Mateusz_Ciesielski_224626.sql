--Imiê i nazwisko: Mateusz Ciesielski
--Numer indeksu: 224626
--Temat bazy danych: Firma Transportowa

--1) Usuwamy star¹ strukturê bazy danych i wszystkie niepotrzebne obiekty
USE firma_transportowa
GO
DROP VIEW Stan_kursow;
GO
DROP PROC info_pracownik;
GO
DROP PROC dodaj_kurs;
GO
DROP FUNCTION raport_pojazd;
GO
DROP FUNCTION bilans_miesiaca;
GO
DROP TABLE Lista_towar;
GO
DROP TABLE Dane_kurs;
GO
DROP TABLE Kurs;
GO
DROP TABLE Klient;
GO
DROP TABLE Lista_pracownikow_magazynu;
GO
DROP TABLE Pracownik;
GO
DROP TABLE Stanowisko;
GO
DROP TABLE Inwentarz_magazynu;
GO
DROP TABLE Towar;
GO
DROP TABLE Typ_towaru;
GO
DROP TABLE Klasa_towaru;
GO
DROP TABLE Magazyn;
GO
DROP TABLE Adres;
GO

DROP TABLE Pojazd;
GO
DROP TABLE Miasto;
GO
DROP TABLE Naczepa;
GO

--2) Tworzymy strukturê bazy danych
CREATE TABLE Naczepa (
  id_Naczepa INTEGER NOT NULL IDENTITY(1,1) PRIMARY KEY,
  marka VARCHAR(20) NOT NULL CHECK(LEN(marka)>2),
  typ VARCHAR(20) NOT NULL,
  ladownosc DECIMAL(8,2) NOT NULL CHECK(ladownosc>0),
  data_prod DATE NOT NULL,
  data_przegladu DATE NOT NULL,
  data_wygasniecia_przegladu DATE NOT NULL,
  dostepny BIT DEFAULT 1
);
GO
CREATE TABLE Miasto (
  id_Miasto INTEGER NOT NULL IDENTITY(1,1) PRIMARY KEY,
  miasto VARCHAR(30) NOT NULL CHECK(LEN(miasto)>1),
  panstwo VARCHAR(30) NOT NULL CHECK(LEN(panstwo)>2),
);
GO
CREATE TABLE Pojazd (
  id_Pojazd INTEGER  NOT NULL IDENTITY(1,1) PRIMARY KEY,
  marka VARCHAR(30) NOT NULL CHECK(LEN(marka)>2),
  typ VARCHAR(30) NOT NULL,
  przebieg INTEGER NOT NULL CHECK(przebieg>=0),
  silnik DECIMAL (8,2)  NOT NULL CHECK(silnik>=0),
  rok_prod DATE NOT NULL,
  VIN CHAR(17) UNIQUE  NOT NULL CHECK(LEN(VIN)=17),
  KM INTEGER  NOT NULL,
  data_przegladu DATE NOT NULL,
  data_wygasniecia_przegladu DATE NOT NULL,
  dostepny BIT DEFAULT 1
);
GO
CREATE TABLE Adres (
  id_Adres INTEGER NOT NULL IDENTITY(1,1) PRIMARY KEY,
  id_Miasto INTEGER NOT NULL REFERENCES Miasto(id_Miasto),
  numer VARCHAR(8) NOT NULL,
  ulica VARCHAR(40) NOT NULL CHECK(LEN(ulica)>2),
  kod CHAR(10) NOT NULL,
);
GO
CREATE TABLE Magazyn (
id_Magazyn INTEGER NOT NULL PRIMARY KEY,
id_Adres INTEGER NOT NULL REFERENCES Adres(id_Adres),
opis VARCHAR(60) NULL,
);
GO
CREATE TABLE Klasa_towaru (
  id_Klasa_towaru INTEGER NOT NULL PRIMARY KEY,
  nazwa VARCHAR(100) NOT NULL CHECK(LEN(nazwa)>2),
  typ_bezp VARCHAR(20) NOT NULL,
);
GO
CREATE TABLE Typ_towaru (
  id_Typ_towaru INTEGER NOT NULL PRIMARY KEY,
  id_Klasa_towaru INTEGER NULL REFERENCES Klasa_towaru(id_Klasa_towaru),
  nazwa VARCHAR(40) NOT NULL CHECK(LEN(nazwa)>2),
  waga DECIMAL(8,2) NOT NULL CHECK(waga>=0),
);
GO
CREATE TABLE Towar (
  id_Towar INTEGER NOT NULL IDENTITY(1,1) PRIMARY KEY,
  id_Typ_towaru INTEGER NOT NULL REFERENCES Typ_towaru(id_Typ_towaru),
  cena DECIMAL(8,2) NOT NULL CHECK(cena>=0),
  podatek INTEGER NOT NULL DEFAULT 23,
);
GO
CREATE TABLE Inwentarz_magazynu (
id_Magazyn INTEGER NOT NULL REFERENCES Magazyn(id_Magazyn),
id_Towar INTEGER NOT NULL REFERENCES Towar(id_Towar),
data_zmagazynowania DATE NOT NULL DEFAULT GETDATE(),
ilosc INTEGER NOT NULL CHECK (ilosc>0),
CONSTRAINT inw_mag_pk PRIMARY KEY(id_Magazyn, id_Towar)
);
GO
CREATE TABLE Stanowisko(
id_Stanowisko INTEGER NOT NULL PRIMARY KEY,
nazwa VARCHAR(20) NOT NULL CHECK(LEN(nazwa)>2),
);
GO
CREATE TABLE Pracownik (
  id_Pracownik INTEGER NOT NULL IDENTITY(1,1) PRIMARY KEY,
  id_Adres INTEGER NOT NULL REFERENCES Adres(id_Adres),
  id_Stanowisko INTEGER NOT NULL REFERENCES Stanowisko(id_Stanowisko),
  imie VARCHAR(30) NOT NULL CHECK(LEN(imie)>2),
  nazwisko VARCHAR(30) NOT NULL CHECK(LEN(nazwisko)>2),
  pesel CHAR(11) UNIQUE NOT NULL CHECK(LEN(pesel)=11),
  pensja DECIMAL(8,2) NULL DEFAULT 1600 CHECK(pensja>=0),
  dodatek DECIMAL(8,2) NOT NULL DEFAULT 0,
  dostepny BIT DEFAULT 1,
  usuniety BIT DEFAULT 0
);
GO
CREATE TABLE Lista_pracownikow_magazynu (
id_Magazyn INTEGER NOT NULL REFERENCES Magazyn(id_Magazyn),
id_Pracownik INTEGER NOT NULL REFERENCES Pracownik(id_Pracownik),
PRIMARY KEY (id_Magazyn, id_Pracownik),
);
GO
CREATE TABLE Klient (
  id_Klient INTEGER NOT NULL IDENTITY(1,1) PRIMARY KEY,
  id_Adres INTEGER NOT NULL REFERENCES Adres(id_Adres),
  nazwa VARCHAR(50) UNIQUE NOT NULL CHECK (LEN(nazwa)>2),
  NIP CHAR(10) NULL CHECK (LEN(NIP)=10),
  nr_konta_IBAN CHAR(32)  NULL,
  nr_konta_NRB CHAR(26)  NULL,
  usuniety BIT DEFAULT 0
);
GO
CREATE TABLE Kurs (
  id_Kurs INTEGER NOT NULL IDENTITY(1,1) PRIMARY KEY,
  id_Adres_zaladunku INTEGER NOT NULL REFERENCES Adres(id_Adres),
  id_Adres_rozladunku INTEGER NOT NULL REFERENCES Adres(id_Adres),
  id_Klient INTEGER  NOT NULL REFERENCES Klient(id_Klient),
  dystans INTEGER NULL CHECK(dystans>0 OR dystans IS NULL),
  wartosc DECIMAL(8,2) NOT NULL CHECK(wartosc>=0),
  data_zaladunku DATETIME NULL,
  data_rozladunku DATETIME NULL,
  data_zaplaty DATETIME NULL,
  zakonczony BIT DEFAULT 0,
  uwagi VARCHAR(200) NULL,
);
GO
CREATE TABLE Dane_kurs (
  id_Kurs INTEGER NOT NULL REFERENCES Kurs(id_Kurs),
  id_Pojazd INTEGER NOT NULL REFERENCES Pojazd(id_Pojazd),
  id_Kierowca INTEGER NOT NULL REFERENCES Pracownik(id_Pracownik),
  id_Naczepa INTEGER NOT NULL REFERENCES Naczepa(id_Naczepa),
  koszt DECIMAL(8,2) NULL CHECK(koszt>=0 OR koszt IS NULL),
  dystans INTEGER NULL CHECK(dystans>0 OR dystans IS NULL),
  zuzyte_paliwo DECIMAL(8,2) NULL CHECK(zuzyte_paliwo>=0 OR zuzyte_paliwo IS NULL),
  uwagi VARCHAR(200) NULL,
  PRIMARY KEY (id_Kurs, id_Pojazd)
);
GO
CREATE TABLE Lista_towar (
  id_Pojazd INTEGER NOT NULL,
  id_Kurs INTEGER NOT NULL,
  id_Towar INTEGER NOT NULL REFERENCES Towar(id_Towar),
  cena_netto DECIMAL(8,2) NOT NULL CHECK (cena_netto>=0),
  podatek INT NOT NULL DEFAULT 23 CHECK(podatek>=0),
  ilosc INT NOT NULL CHECK (ilosc>0),
  FOREIGN KEY (id_Kurs,id_Pojazd) REFERENCES Dane_Kurs(id_Kurs,id_Pojazd), 
  CONSTRAINT Lista_towar_pk PRIMARY KEY (id_Pojazd, id_Kurs, id_Towar)
);
--3) Dodajemy przyk³adowe rekordy
INSERT INTO Miasto(miasto, panstwo) VALUES('Warszawa','Polska');
INSERT INTO Miasto(miasto, panstwo) VALUES('Kraków','Polska');
INSERT INTO Miasto(miasto, panstwo) VALUES('Poznañ','Polska');
INSERT INTO Miasto(miasto, panstwo) VALUES('Gdañsk','Polska');
INSERT INTO Miasto(miasto, panstwo) VALUES('Katowice','Polska');
INSERT INTO Miasto(miasto, panstwo) VALUES('Brema','Niemcy');
INSERT INTO Miasto(miasto, panstwo) VALUES('Lubeka','Niemcy');
INSERT INTO Miasto(miasto, panstwo) VALUES('Frankfurt nad Menem','Niemcy');
INSERT INTO Miasto(miasto, panstwo) VALUES('Antwerpia','Belgia');
INSERT INTO Miasto(miasto, panstwo) VALUES('Amsterdam','Holandia');
INSERT INTO Miasto(miasto, panstwo) VALUES('Sztokholm','Szwecja');
INSERT INTO Miasto(miasto, panstwo) VALUES('Berlin','Niemcy');

INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (1, '14', 'Mazowiecka', '00-130');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (1, '6', 'Armii Krajowej', '00-145');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (2, '13', 'W³adys³awa £okietka', '30-124');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (2, '67', 'Stefana Batorego', '30-150');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (3, '3', 'Piernikowa', '60-080');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (3, '19', 'Kolejarska', '60-230');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (4, '4', 'Jaœkowa Dolina', '80-005');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (5, '24', 'Weglowa', '40-354');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (6, '7', 'Am Markt', '2801');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (7, '123', 'Hanseatischer Platz', '23552');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (7, '35', 'Friedrich Barbarossa Strasse', '23553');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (7, '123', 'Hansa Strasse', '23552');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (8, '40', 'Unter Vogeln', '60310');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (9, '1', 'Hoboken', '2045');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (10, '76', 'Vijzel', '1024');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (11, '45', 'Vargattan', '1236');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (12, '66/7', 'Kurfurtstendamm', '12567');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (1, '3', 'Obroncow Tobruku', '00-542');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (5, '73', 'Brunatna', '40-542');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (1, '45', 'Lewa', '00-427');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (4, '5', 'Spichrzowa', '80-111');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (4, '7', 'Kaperska', '80-132');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (4, '6', 'Prawa', '80-143');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (4, '1', 'Prosta', '80-123');
INSERT INTO Adres(id_Miasto, numer, ulica, kod) VALUES (5, '11', 'Okopowa', '40-123');

INSERT INTO Klient(id_Adres, nazwa,nr_konta_IBAN) VALUES (10, 'Hans und Klaus GmbH', 'DE54694860382758391748');
INSERT INTO Klient(id_Adres, nazwa, NIP, nr_konta_NRB) VALUES (1, 'WarChem Sp. z o.o.','4614353534', '80124046970375491234643657');
INSERT INTO Klient(id_Adres, nazwa, NIP) VALUES (3, 'Feanor S.A.', '7613383904');
INSERT INTO Klient(id_Adres, nazwa, nr_konta_IBAN) VALUES (17, 'Sigurd-Logistik Aktiengesellschaft', 'DE416948260982750331490');
INSERT INTO Klient(id_Adres, nazwa, NIP) VALUES (18, 'FerraMet Sp. z o.o.', '3273239895');

INSERT INTO Stanowisko (id_Stanowisko, nazwa) VALUES (1, 'Kierowca');
INSERT INTO Stanowisko (id_Stanowisko, nazwa) VALUES (2, 'Magazynier');
INSERT INTO Stanowisko (id_Stanowisko, nazwa) VALUES (3, 'Dyspozytor');
INSERT INTO Stanowisko (id_Stanowisko, nazwa) VALUES (4, 'Kierownik magazynu');
INSERT INTO Stanowisko (id_Stanowisko, nazwa) VALUES (5, 'Pracownik biurowy');

INSERT INTO Pracownik (id_Stanowisko, id_Adres, nazwisko, imie, pesel, pensja) VALUES (1, 7, 'Banan', 'Jerzy', '86200407043', 2000);
INSERT INTO Pracownik (id_Stanowisko, id_Adres, nazwisko, imie, pesel) VALUES (1, 6, 'Czapla', 'Tadeusz', '75100347691');
INSERT INTO Pracownik (id_Stanowisko, id_Adres, nazwisko, imie, pesel) VALUES (1, 1, 'Ostrogski', 'Wac³aw', '82150647073');
INSERT INTO Pracownik (id_Stanowisko, id_Adres, nazwisko, imie, pesel) VALUES (2, 2, 'Palipies', 'Janusz', '76170407095');
INSERT INTO Pracownik (id_Stanowisko, id_Adres, nazwisko, imie, pesel, pensja) VALUES (4, 3, 'Niedzwiedz', 'Stefan', '84030509011', 3000);
INSERT INTO Pracownik (id_Stanowisko, id_Adres, nazwisko, imie, pesel) VALUES (1, 19, 'Potocki', 'Micha³', '89070908767');
INSERT INTO Pracownik (id_Stanowisko, id_Adres, nazwisko, imie, pesel) VALUES (5, 20, 'Tarnowski', 'Jerzy', '88050208654');
INSERT INTO Pracownik (id_Stanowisko, id_Adres, nazwisko, imie, pesel) VALUES (2, 21, 'Wiœniowiecki', 'Roman', '84020108678');
INSERT INTO Pracownik (id_Stanowisko, id_Adres, nazwisko, imie, pesel) VALUES (2, 22, 'Opolczyk', 'Zygfryd', '90010107653');
INSERT INTO Pracownik (id_Stanowisko, id_Adres, nazwisko, imie, pesel, pensja) VALUES (4, 23, 'Tabaka', 'Jan', '80050908432', 3000);
INSERT INTO Pracownik (id_Stanowisko, id_Adres, nazwisko, imie, pesel) VALUES (2, 24, 'Ko³o', 'Jan', '88030106789');

INSERT INTO Pojazd (marka, typ, przebieg, silnik, rok_prod, VIN, KM, data_przegladu,data_wygasniecia_przegladu) VALUES ('Scania', 'R-500', 12456, 16, '2010-02-01', '1M8GDM9A_KP042788', 500,'2014-04-01','2015-04-01' );
INSERT INTO Pojazd (marka, typ, przebieg, silnik, rok_prod, VIN, KM, data_przegladu,data_wygasniecia_przegladu) VALUES ('Scania', 'R-500', 78219, 16, '2009-10-12', '1M8GDM9A_KP145890', 500,'2014-04-01', '2015-04-01');
INSERT INTO Pojazd (marka, typ, przebieg, silnik, rok_prod, VIN, KM, data_przegladu,data_wygasniecia_przegladu) VALUES ('MAN', 'TGA', 123798, 12, '2007-05-19', '1N8GTM6B_KP047893', 430,'2014-03-15', '2015-03-15');
INSERT INTO Pojazd (marka, typ, przebieg, silnik, rok_prod, VIN, KM, data_przegladu,data_wygasniecia_przegladu) VALUES ('MAN', 'TGA', 156129, 12, '2007-03-10', '1N8GTM6B_KP123487', 430,'2014-03-15', '2015-03-15');
INSERT INTO Pojazd (marka, typ, przebieg, silnik, rok_prod, VIN, KM, data_przegladu,data_wygasniecia_przegladu) VALUES ('MAN', 'TGX', 194982, 12, '2007-03-10', '1N8GTM6B_KP548730', 540,'2014-02-09', '2015-02-09');
INSERT INTO Pojazd (marka, typ, przebieg, silnik, rok_prod, VIN, KM, data_przegladu,data_wygasniecia_przegladu) VALUES ('DAF', 'XF 105', 48723, 14, '2006-02-10', '2Y3TTE6B_KP432334', 560,'2013-12-01', '2014-02-02');

INSERT INTO Naczepa(marka, typ, ladownosc, data_prod, data_przegladu,data_wygasniecia_przegladu) VALUES ('Wielton', 'N S 3 SP M2', 2000, '2007-03-20', '2014-02-09','2015-02-09' );
INSERT INTO Naczepa(marka, typ, ladownosc, data_prod, data_przegladu,data_wygasniecia_przegladu) VALUES ('Wielton', 'N S 3 SP M2', 2000, '2007-03-20', '2014-02-09', '2015-02-09');
INSERT INTO Naczepa(marka, typ, ladownosc, data_prod, data_przegladu,data_wygasniecia_przegladu) VALUES ('Wielton', 'N S 3 SP M2', 2000, '2007-03-20', '2014-02-09', '2015-02-09');
INSERT INTO Naczepa(marka, typ, ladownosc, data_prod, data_przegladu,data_wygasniecia_przegladu) VALUES ('Janmil', '1500-100-900', 3000, '2009-05-11', '2014-03-17', '2015-04-17');
INSERT INTO Naczepa(marka, typ, ladownosc, data_prod, data_przegladu,data_wygasniecia_przegladu) VALUES ('Janmil', '1500-100-900', 3000, '2009-05-11', '2014-03-17', '2015-03-17');
INSERT INTO Naczepa(marka, typ, ladownosc, data_prod, data_przegladu,data_wygasniecia_przegladu) VALUES ('Schmitz', 'SteinStark', 3000, '2009-05-11', '2013-12-11', '2014-01-17');

INSERT INTO Kurs (id_Adres_zaladunku, id_Adres_rozladunku, id_Klient, dystans, wartosc, data_zaladunku, data_rozladunku, data_zaplaty, zakonczony) VALUES (8, 15, 4, 794, 25416.23, '2014-03-14', '2014-03-17', '2014-03-19',1);
INSERT INTO Kurs (id_Adres_zaladunku, id_Adres_rozladunku, id_Klient, dystans, wartosc, data_zaladunku, data_rozladunku, data_zaplaty, zakonczony) VALUES (8, 16, 5,476, 17867.34, '2014-01-11', '2014-01-13', '2014-01-22',1);
INSERT INTO Kurs (id_Adres_zaladunku, id_Adres_rozladunku, id_Klient, dystans, wartosc, data_zaladunku) VALUES (1, 14, 2,653, 12854.67, '2014-05-05');
INSERT INTO Kurs (id_Adres_zaladunku, id_Adres_rozladunku, id_Klient, dystans, wartosc, data_zaladunku, data_rozladunku, data_zaplaty, zakonczony) VALUES (8, 20, 5,494, 27867.23, '2014-01-11', '2014-01-13', '2014-01-22',1);
INSERT INTO Kurs (id_Adres_zaladunku, id_Adres_rozladunku, id_Klient, dystans, wartosc, data_zaladunku, data_rozladunku) VALUES (21, 13, 1, 768, 38786.89, '2014-05-02', '2014-05-04');

INSERT INTO Dane_Kurs (id_Kurs, id_Pojazd, id_Kierowca, id_Naczepa, koszt, dystans, zuzyte_paliwo) VALUES (1, 1, 2, 3, 1254.12, 812, 100);
INSERT INTO Dane_Kurs (id_Kurs, id_Pojazd, id_Kierowca, id_Naczepa, koszt, dystans, zuzyte_paliwo) VALUES (1, 2, 1, 2, 1283.78, 820, 110);
INSERT INTO Dane_Kurs (id_Kurs, id_Pojazd, id_Kierowca, id_Naczepa, koszt, dystans, zuzyte_paliwo) VALUES (1, 3, 3, 1, 1218.08, 808, 95);
INSERT INTO Dane_Kurs (id_Kurs, id_Pojazd, id_Kierowca, id_Naczepa, koszt, dystans, zuzyte_paliwo) VALUES (2, 4, 5, 5, 1400.12, 500, 45);
INSERT INTO Dane_Kurs (id_Kurs, id_Pojazd, id_Kierowca, id_Naczepa, koszt, dystans, zuzyte_paliwo) VALUES (3, 5, 2, 4, 1321.54, 780, 93);
INSERT INTO Dane_Kurs (id_Kurs, id_Pojazd, id_Kierowca, id_Naczepa, koszt, dystans, zuzyte_paliwo) VALUES (3, 1, 6, 3, 1324.21, 780, 93);
INSERT INTO Dane_Kurs (id_Kurs, id_Pojazd, id_Kierowca, id_Naczepa, koszt, dystans, zuzyte_paliwo) VALUES (3, 3, 1, 2, 1467.23, 784, 95);
INSERT INTO Dane_Kurs (id_Kurs, id_Pojazd, id_Kierowca, id_Naczepa, koszt, dystans, zuzyte_paliwo) VALUES (4, 2, 2, 4, 1411.54, 500, 46);
INSERT INTO Dane_Kurs (id_Kurs, id_Pojazd, id_Kierowca, id_Naczepa, koszt, dystans, zuzyte_paliwo) VALUES (5, 1, 1, 1, 1432.32, 775, 91);
INSERT INTO Dane_Kurs (id_Kurs, id_Pojazd, id_Kierowca, id_Naczepa, koszt, dystans, zuzyte_paliwo) VALUES (5, 5, 6, 3, 1321.12, 772, 88);
INSERT INTO Dane_Kurs (id_Kurs, id_Pojazd, id_Kierowca, id_Naczepa, koszt, dystans, zuzyte_paliwo) VALUES (5, 3, 2, 5, 1299.65, 773, 87);
INSERT INTO Dane_Kurs (id_Kurs, id_Pojazd, id_Kierowca, id_Naczepa, koszt, dystans, zuzyte_paliwo) VALUES (5, 4, 3, 2, 1467.11, 776, 93);

INSERT INTO Magazyn (id_Magazyn, id_Adres, opis) VALUES (1, 8, 'Magazyn wyrobów metalurgicznych');
INSERT INTO Magazyn (id_Magazyn, id_Adres, opis) VALUES (2, 1, 'Magazyn WarChem');
INSERT INTO Magazyn (id_Magazyn, id_Adres, opis) VALUES (3, 18, 'Magazyn FerraMet');
INSERT INTO Magazyn (id_Magazyn, id_Adres, opis) VALUES (4,21, 'Magazyn wyrobów metalurgicznych i chemicznych');
INSERT INTO Magazyn (id_Magazyn, id_Adres, opis) VALUES (5,3, 'Magazyn Feanor');

INSERT INTO Lista_pracownikow_magazynu(id_Magazyn, id_Pracownik) VALUES (1, 4);
INSERT INTO Lista_pracownikow_magazynu(id_Magazyn, id_Pracownik) VALUES (1, 5);
INSERT INTO Lista_pracownikow_magazynu(id_Magazyn, id_Pracownik) VALUES (1, 8);
INSERT INTO Lista_pracownikow_magazynu(id_Magazyn, id_Pracownik) VALUES (4, 9);
INSERT INTO Lista_pracownikow_magazynu(id_Magazyn, id_Pracownik) VALUES (4, 10);
INSERT INTO Lista_pracownikow_magazynu(id_Magazyn, id_Pracownik) VALUES (4, 11);

INSERT INTO Klasa_towaru(id_Klasa_towaru, nazwa,typ_bezp) VALUES (1, 'Wyroby metalurgiczne kl.A', 'A');
INSERT INTO Klasa_towaru(id_Klasa_towaru, nazwa,typ_bezp) VALUES (2, 'Wyroby chemiczne kl. B', 'B');
INSERT INTO Klasa_towaru(id_Klasa_towaru, nazwa,typ_bezp) VALUES (3, 'Wyroby chemiczne kl. C', 'C');
INSERT INTO Klasa_towaru(id_Klasa_towaru, nazwa,typ_bezp) VALUES (4, 'Artyku³y RTV', 'A');
INSERT INTO Klasa_towaru(id_Klasa_towaru, nazwa,typ_bezp) VALUES (5, 'AGD', 'A');

INSERT INTO Typ_towaru(id_Typ_towaru, id_Klasa_towaru, nazwa,waga) VALUES(1,1,'Blacha stalowa 1m x 1m', 15);
INSERT INTO Typ_towaru(id_Typ_towaru, id_Klasa_towaru, nazwa,waga) VALUES(2,2,'Beczka Azotanu amonu', 30);
INSERT INTO Typ_towaru(id_Typ_towaru, id_Klasa_towaru, nazwa,waga) VALUES(3,3,'Beczka Bromowodoru', 30);
INSERT INTO Typ_towaru(id_Typ_towaru, id_Klasa_towaru, nazwa,waga) VALUES(4,4,'Telewizor LG L540', 14);
INSERT INTO Typ_towaru(id_Typ_towaru, id_Klasa_towaru, nazwa,waga) VALUES(5,1,'Surówka hutnicza', 80);

INSERT INTO Towar(id_Typ_towaru, cena) VALUES(1, 1500);
INSERT INTO Towar(id_Typ_towaru, cena) VALUES(2, 800);
INSERT INTO Towar(id_Typ_towaru, cena) VALUES(3, 700);
INSERT INTO Towar(id_Typ_towaru, cena) VALUES(4, 3000);
INSERT INTO Towar(id_Typ_towaru, cena) VALUES(5, 2500);
INSERT INTO Towar(id_Typ_towaru, cena) VALUES(2, 850);
INSERT INTO Towar(id_Typ_towaru, cena) VALUES(3, 750);

INSERT INTO Inwentarz_magazynu (id_Magazyn, id_Towar, data_zmagazynowania, ilosc) VALUES(1,1, '2014-02-11', 10000);
INSERT INTO Inwentarz_magazynu (id_Magazyn, id_Towar, data_zmagazynowania, ilosc) VALUES(4,2, '2013-05-04', 5000);
INSERT INTO Inwentarz_magazynu (id_Magazyn, id_Towar, data_zmagazynowania, ilosc) VALUES(4,3, '2013-05-04', 8000);
INSERT INTO Inwentarz_magazynu (id_Magazyn, id_Towar, data_zmagazynowania, ilosc) VALUES(1,2, '2013-05-04', 5000);
INSERT INTO Inwentarz_magazynu (id_Magazyn, id_Towar, data_zmagazynowania, ilosc) VALUES(1,5, '2014-02-14', 10000);
INSERT INTO Inwentarz_magazynu (id_Magazyn, id_Towar, data_zmagazynowania, ilosc) VALUES(4,5, '2013-04-07', 5000);

INSERT INTO Lista_towar(id_Pojazd, id_Kurs, id_Towar, cena_netto, ilosc) VALUES(1,1,1, 1500, 215);
INSERT INTO Lista_towar(id_Pojazd, id_Kurs, id_Towar, cena_netto, ilosc) VALUES(2,1,1, 1500, 215);
INSERT INTO Lista_towar(id_Pojazd, id_Kurs, id_Towar, cena_netto, ilosc) VALUES(3,1,1, 1500, 200);
INSERT INTO Lista_towar(id_Pojazd, id_Kurs, id_Towar, cena_netto, ilosc) VALUES(4,2,1, 1500, 200);
INSERT INTO Lista_towar(id_Pojazd, id_Kurs, id_Towar, cena_netto, ilosc) VALUES(5,5,1, 1500, 200);
GO

--4a) Tworzymy widok 
--Widok "Stan_Kursow" umo¿liwiaj¹cy podgl¹d najwa¿niejszych informacji o wszystkich kursach.
CREATE VIEW Stan_kursow
AS 
SELECT k.id_Kurs ,k.zakonczony, k.wartosc,
(SELECT SUM(koszt)from Dane_kurs
GROUP BY id_Kurs
HAVING id_Kurs=k.id_Kurs)AS koszt,
(CASE WHEN k.wartosc>
4*(SELECT SUM(koszt)from Dane_kurs
GROUP BY id_Kurs
HAVING id_Kurs=k.id_Kurs) THEN 'wysoka'
WHEN k.wartosc>
2*(SELECT SUM(koszt)from Dane_kurs
GROUP BY id_Kurs
HAVING id_Kurs=k.id_Kurs) THEN 'srednia'
ELSE 'niska'
END)AS stopa_zwrotu,
k.data_zaladunku,
(SELECT(a.ulica+' '+a.numer+', '+m.miasto+', '+m.panstwo)
FROM Adres a JOIN Miasto m
ON a.id_Miasto=m.id_Miasto
 WHERE a.id_Adres=k.id_Adres_zaladunku)
 AS adres_zaladunku,
k.data_rozladunku,
(SELECT(a.ulica+' '+a.numer+', '+m.miasto+', '+m.panstwo)
FROM Adres a JOIN Miasto m
ON a.id_Miasto=m.id_Miasto
 WHERE a.id_Adres=k.id_Adres_rozladunku)
AS adres_rozladunku ,
(SELECT COUNT(id_Pojazd) from Dane_kurs
GROUP BY id_Kurs
HAVING id_Kurs=k.id_Kurs)AS Liczba_pojazdow,
(SELECT SUM(zuzyte_paliwo)from Dane_kurs
GROUP BY id_Kurs
HAVING id_Kurs=k.id_Kurs)
AS zuzyte_paliwo,
k.dystans
FROM
Kurs k;
GO

--4b) Sprawdzenie, ¿e widok dzia³a
SELECT * from Stan_kursow;
GO

--5a) Tworzymy funkcjê 1
--Funkcja "raport_pojazd" wyswietla tabele daj¹c¹ informacjê o eksploatacji
-- pojazdu o ID podanym w parametrze funkcji.
CREATE FUNCTION raport_pojazd(@id_pojazd INT)
RETURNS TABLE AS
RETURN (SELECT p.id_Pojazd,p.marka, p.typ,(COUNT(d.id_Pojazd))AS ilosc_kursow, (SUM(d.dystans))AS calkowity_dystans  
, (SUM(d.zuzyte_paliwo))AS calkowite_zuzycie_paliwa, (SUM(d.zuzyte_paliwo)*100/SUM(d.dystans)) AS srednie_zuzycie_paliwa_na_100km,
(SUM (d.koszt))AS calkowity_koszt
from Pojazd p join Dane_kurs d
on d.id_Pojazd=p.id_Pojazd 
GROUP BY p.id_Pojazd, p.marka, p.typ
HAVING p.id_Pojazd=@id_pojazd);
GO

--5b) Sprawdzenie, ¿e funkcja 1 dzia³a
SELECT * FROM raport_pojazd(1);
GO

--6a) Tworzymy funkcjê 2
--Funkcja "bilans_miesiaca zwraca zmienna @bilans równ¹ zyskowi z kursów 
--pomniejszon¹ o wyp³aty dla pracowników i koszty kursów w przedziale miesi¹ca
--podanej jako parametr funkcji
CREATE FUNCTION bilans_miesiaca(@rok INT, @miesiac INT)
RETURNS DECIMAL (8,2)
BEGIN
DECLARE @bilans DECIMAL(8,2)
DECLARE @wartosc_kursow DECIMAL (8,2)
DECLARE @pensje_pracownikow DECIMAL (8,2)
DECLARE @koszt_kursow DECIMAL (8,2)
DECLARE @id_kurs INT
DECLARE kursor_koszt CURSOR
FOR SELECT id_Kurs FROM Kurs

SET @wartosc_kursow=(
(SELECT SUM(wartosc) from Kurs
GROUP BY MONTH(data_zaplaty), YEAR(data_zaplaty)
HAVING YEAR(data_zaplaty)=@rok AND MONTH(data_zaplaty)=@miesiac))

SET @pensje_pracownikow=
(SELECT SUM(p.pensja) from Pracownik p 
GROUP BY usuniety
HAVING usuniety=0)

SET @koszt_kursow=0
OPEN kursor_koszt
FETCH NEXT FROM kursor_koszt INTO @id_kurs
	WHILE (@@FETCH_STATUS=0)
	BEGIN
	IF((SELECT MONTH(data_zaplaty) FROM Kurs where id_Kurs=@id_kurs)=@miesiac
	AND (SELECT YEAR(data_zaplaty) FROM Kurs where id_Kurs=@id_kurs)=@rok)
	SET @koszt_kursow=@koszt_kursow+(SELECT SUM(koszt) FROM Dane_kurs GROUP BY id_Kurs HAVING id_Kurs=@id_kurs)
	FETCH NEXT FROM kursor_koszt INTO @id_kurs
	END

CLOSE kursor_koszt
DEALLOCATE kursor_koszt

SET @bilans=(COALESCE(@wartosc_kursow,0)-COALESCE(@pensje_pracownikow,0)-COALESCE(@koszt_kursow,0))
RETURN @bilans
END;
GO

--6b) Sprawdzenie, ¿e funkcja 2 dzia³a
SELECT dbo.bilans_miesiaca(2014,3);
GO

--7a) Tworzymy procedurê 1
--Procedura "info_pracownik" podajê informacjê o pojedyñczym pracowniku, jeœli 
--jako parametr podano ID lub pesel lub imiê i nazwisko pracownika.
--Wyœwietlane dane s¹ uzale¿nionie od stanowiska pracownika.
--Jeœli w parametrze podano nazwê lub ID stanowiska
-- przedstawiane jest zestawienie informacji o wszystkich pracownikach na tym stanowisku.
CREATE PROC info_pracownik
@id_pracownik INT=NULL,
@imie VARCHAR(30)=NULL,
@nazwisko VARCHAR(30)=NULL,
@pesel CHAR(11)=NULL,
@stanowisko VARCHAR(20)=NULL,
@id_stanowisko INT=NULL
AS
BEGIN
IF (@stanowisko IS NOT NULL)
BEGIN
IF(EXISTS(SELECT id_Stanowisko from Stanowisko where nazwa=@stanowisko))
BEGIN
SET @id_stanowisko=(SELECT id_Stanowisko from Stanowisko where nazwa=@stanowisko)
IF(@id_stanowisko=1)
BEGIN
SELECT p.imie, p.nazwisko, p.pensja,p.dodatek, p.pesel, 
(SELECT a.ulica+' '+a.numer+', '+m.miasto 
from Adres a join Miasto m
ON a.id_Miasto=m.id_Miasto
WHERE a.id_Adres=p.id_Adres)
AS adres,
(SELECT (SUM(zuzyte_paliwo)*100/SUM(dystans)) from Dane_kurs
WHERE Dane_kurs.id_Kierowca=p.id_Pracownik)
AS srednie_zuzycie_paliwa_na_100km from Pracownik p
WHERE p.id_Stanowisko=@id_stanowisko
END
IF(@id_stanowisko=2)
BEGIN
SELECT p.imie, p.nazwisko, p.pensja,p.dodatek, p.pesel, 
(SELECT a.ulica+' '+a.numer+', '+m.miasto 
from Adres a join Miasto m
ON a.id_Miasto=m.id_Miasto
WHERE a.id_Adres=p.id_Adres)
AS adres,
(SELECT a.ulica+' '+a.numer+', '+m.miasto from Adres a join Miasto m
ON a.id_Miasto=m.id_Miasto join Magazyn mag
on mag.id_Adres=a.id_Adres
join Lista_pracownikow_magazynu l
on l.id_Magazyn=mag.id_Magazyn
WHERE l.id_Pracownik=p.id_Pracownik )
AS miejsce_pracy_magazyniera from Pracownik p
WHERE p.id_Stanowisko=@id_stanowisko
END
IF(@id_stanowisko>=3)
BEGIN
SELECT p.imie, p.nazwisko, p.pensja,p.dodatek, p.pesel, 
(SELECT a.ulica+' '+a.numer+', '+m.miasto 
from Adres a join Miasto m
ON a.id_Miasto=m.id_Miasto
WHERE a.id_Adres=p.id_Adres)
AS adres
from Pracownik p
WHERE p.id_Stanowisko=@id_stanowisko
END
END
ELSE
PRINT 'Brak stanowiska o podanej nazwie.'
END
ELSE
BEGIN
IF(@id_pracownik IS NOT NULL AND NOT EXISTS((SELECT id_Pracownik from Pracownik where @id_pracownik=id_Pracownik)))
PRINT 'Brak pracownika o podanym numerze ID.'
IF(@id_pracownik IS NULL AND NOT EXISTS((SELECT id_Pracownik from Pracownik where @imie=imie AND @nazwisko=nazwisko)))
PRINT 'Brak pracownika o podanym imieniu i nazwisku.'
ELSE IF (@id_pracownik IS NULL)
SET @id_pracownik=(SELECT id_Pracownik from Pracownik where @imie=imie AND @nazwisko=nazwisko)

IF(@id_pracownik IS NULL AND NOT EXISTS((SELECT id_Pracownik from Pracownik where @pesel=pesel)))
PRINT 'Brak pracownika o podanym numerze pesel.'
ELSE IF (@id_pracownik IS NULL)
SET @id_pracownik=(SELECT id_Pracownik from Pracownik where @pesel=pesel)
IF ((SELECT id_Stanowisko from Pracownik where
id_Pracownik=@id_pracownik)=1) 
BEGIN
SELECT p.imie, p.nazwisko, p.pensja,p.dodatek, p.pesel, 
(SELECT a.ulica+' '+a.numer+', '+m.miasto 
from Adres a join Miasto m
ON a.id_Miasto=m.id_Miasto
WHERE a.id_Adres=p.id_Adres)
AS adres,
(SELECT (SUM(zuzyte_paliwo)*100/SUM(dystans)) from Dane_kurs
WHERE Dane_kurs.id_Kierowca=@id_pracownik)
AS srednie_zuzycie_paliwa_na_100km from Pracownik p
WHERE p.id_Pracownik=@id_pracownik
END
ELSE IF ((SELECT id_Stanowisko from Pracownik where
id_Pracownik=@id_pracownik)=2)
BEGIN
SELECT p.imie, p.nazwisko, p.pensja,p.dodatek, p.pesel, 
(SELECT a.ulica+' '+a.numer+', '+m.miasto 
from Adres a join Miasto m
ON a.id_Miasto=m.id_Miasto
WHERE a.id_Adres=p.id_Adres)
AS adres,
(SELECT a.ulica+' '+a.numer+', '+m.miasto from Adres a join Miasto m
ON a.id_Miasto=m.id_Miasto join Magazyn mag
on mag.id_Adres=a.id_Adres
join Lista_pracownikow_magazynu l
on l.id_Magazyn=mag.id_Magazyn
WHERE l.id_Pracownik=@id_pracownik )
AS miejsce_pracy_magazyniera from Pracownik p
WHERE p.id_Pracownik=@id_pracownik
END
ELSE IF ((SELECT id_Stanowisko from Pracownik where
id_Pracownik=@id_pracownik)>=3)
BEGIN
SELECT p.imie, p.nazwisko, p.pensja,p.dodatek, p.pesel, 
(SELECT a.ulica+' '+a.numer+', '+m.miasto 
from Adres a join Miasto m
ON a.id_Miasto=m.id_Miasto
WHERE a.id_Adres=p.id_Adres)
AS adres
from Pracownik p
WHERE p.id_Pracownik=@id_pracownik
END
END
END
GO

--7b) Sprawdzenie, ¿e procedura 1 dzia³a
EXEC info_pracownik @stanowisko='Kierowca';
EXEC info_pracownik @pesel='86200407043';
GO

--8a) Tworzymy procedurê 2
--Procedura "dodaj_kurs" umo¿liwia automatyczny dobór
--kierowców, naczep i samochodów do kursu w zale¿noœci od podanej iloœci pojazdów.
--Procedura dodaje odpowiednie rekordy w tabeli Kurs i Dane_Kurs i modyfikuje tabele
--Pojazd, Pracownik i Naczepa.
CREATE PROC dodaj_kurs
@id_klient INT=NULL,
@klient VARCHAR(20)=NULL,
@id_adres_rozladunku INT=NULL,
@id_magazyn INT=NULL,
@magazyn VARCHAR(40)=NULL,
@id_adres_zaladunku INT=NULL,
@dystans INT=NULL,
@wartosc INT=0,
@ilosc_pojazdow INT

AS
BEGIN

IF(@id_klient IS NULL AND @klient IS NOT NULL)
SET @id_klient=(SELECT id_Klient from Klient where nazwa=@klient)
IF(@id_klient IS NULL)
PRINT 'Nie podano klienta.'

IF(@id_magazyn IS NULL AND @magazyn IS NOT NULL)
SET @id_magazyn=(SELECT id_Magazyn from Magazyn where opis=@magazyn)
IF(@id_magazyn IS NULL)
PRINT 'Nie podano magazynu.'

IF(@id_adres_zaladunku IS NULL AND @id_magazyn IS NOT NULL)
SET @id_adres_zaladunku=(SELECT id_Adres from Magazyn where id_Magazyn=@id_magazyn)

IF(@id_adres_rozladunku IS NULL AND @id_klient IS NOT NULL)
SET @id_adres_rozladunku=(SELECT id_Adres from Klient where id_Klient=@id_klient)


IF(@ilosc_pojazdow IS NULL)
PRINT 'Nie podano ilosci pojazdow.'

IF(@id_klient IS NOT NULL AND @id_magazyn IS NOT NULL AND @ilosc_pojazdow IS NOT NULL)
BEGIN

DECLARE @licznik INT=0
DECLARE @temp_id INT=0
DECLARE @temp_dostepny BIT
DECLARE @temp_tab TABLE(id_Licznik INT,id_Pojazd INT, id_Kierowca INT, id_Naczepa INT)


DECLARE kursor_pojazd CURSOR
FOR SELECT id_Pojazd, dostepny FROM Pojazd
OPEN kursor_pojazd
FETCH NEXT FROM kursor_pojazd INTO @temp_id, @temp_dostepny

	WHILE(@licznik<@ilosc_pojazdow AND @@FETCH_STATUS=0)
	BEGIN
		IF(@temp_dostepny=1)
		BEGIN
		INSERT INTO @temp_tab(id_Licznik, id_Pojazd) VALUES (@licznik,@temp_id)
		SET @licznik=@licznik+1
		END
	FETCH NEXT FROM kursor_pojazd INTO @temp_id, @temp_dostepny
	END

CLOSE kursor_pojazd
DEALLOCATE kursor_pojazd
IF(@licznik!=@ilosc_pojazdow)
PRINT 'Za malo dostepnych pojazdow'

ELSE
BEGIN

SET @licznik=0
DECLARE kursor_pracownik CURSOR
FOR SELECT id_Pracownik, dostepny FROM Pracownik WHERE id_Stanowisko=1
OPEN kursor_pracownik
FETCH NEXT FROM kursor_pracownik INTO @temp_id, @temp_dostepny

	WHILE(@licznik<@ilosc_pojazdow AND @@FETCH_STATUS=0)
	BEGIN
		IF(@temp_dostepny=1)
		BEGIN
		UPDATE @temp_tab SET id_Kierowca=@temp_id WHERE id_Licznik=@licznik
		SET @licznik=@licznik+1
		END
	FETCH NEXT FROM kursor_pracownik INTO @temp_id, @temp_dostepny
	END

CLOSE kursor_pracownik
DEALLOCATE kursor_pracownik
IF(@licznik!=@ilosc_pojazdow)
PRINT 'Za malo dostepnych pracowników.'

ELSE
BEGIN

SET @licznik=0
DECLARE kursor_naczepa CURSOR
FOR SELECT id_Naczepa, dostepny FROM Naczepa
OPEN kursor_naczepa
FETCH NEXT FROM kursor_naczepa INTO @temp_id, @temp_dostepny

	WHILE(@licznik<@ilosc_pojazdow AND @@FETCH_STATUS=0)
	BEGIN
		IF(@temp_dostepny=1)
		BEGIN
		UPDATE @temp_tab SET id_Naczepa=@temp_id WHERE id_Licznik=@licznik
		SET @licznik=@licznik+1
		END
	FETCH NEXT FROM kursor_naczepa INTO @temp_id, @temp_dostepny
	END

CLOSE kursor_naczepa
DEALLOCATE kursor_naczepa
IF(@licznik!=@ilosc_pojazdow)
PRINT 'Za malo dostepnych naczep.'

ELSE
BEGIN
DECLARE @Kurs_id INT
INSERT INTO Kurs(id_Adres_zaladunku, id_Adres_rozladunku, id_Klient, wartosc, dystans) VALUES (@id_adres_zaladunku, @id_adres_rozladunku, @id_klient,@wartosc, @dystans)
SET @Kurs_id=@@IDENTITY

DECLARE @temp_id_Pojazd INT
DECLARE @temp_id_Naczepa INT
DECLARE @temp_id_Kierowca INT
DECLARE kursor_dane_kurs CURSOR
FOR SELECT id_Pojazd, id_Kierowca, id_Naczepa FROM @temp_tab
OPEN kursor_dane_kurs
FETCH NEXT FROM kursor_dane_kurs INTO @temp_id_Pojazd, @temp_id_Kierowca, @temp_id_Naczepa
	WHILE(@@FETCH_STATUS=0)
	BEGIN
	INSERT INTO Dane_Kurs(id_Kurs, id_Pojazd, id_Kierowca, id_Naczepa) VALUES (@Kurs_id,@temp_id_Pojazd, @temp_id_Kierowca, @temp_id_Naczepa)
	UPDATE Pojazd SET dostepny=0 WHERE id_Pojazd=@temp_id_Pojazd
	UPDATE Pracownik SET dostepny=0 WHERE id_Pracownik=@temp_id_Kierowca
	UPDATE Naczepa SET dostepny=0 WHERE id_Naczepa=@temp_id_Naczepa
	FETCH NEXT FROM kursor_dane_kurs INTO @temp_id_Pojazd, @temp_id_Kierowca, @temp_id_Naczepa
	END

CLOSE kursor_dane_kurs
DEALLOCATE kursor_dane_kurs
END

END

END

END

END;
GO

--8b) Sprawdzenie, ¿e procedura 2 dzia³a
EXEC dodaj_kurs @id_klient=4, @id_magazyn=1, @ilosc_pojazdow=2;
SELECT * FROM Kurs;
GO

--9a) Tworzymy wyzwalacz 1
--Wyzwalacz "pracownik_delete" uniemo¿liwia usuniêcie wiêcej ni¿ jednego pracownika,
--a zamiast usuwania informacji o pracowniku z bazy danych zmienia wartoœæ atrybutu
--usuniêty na 1.
CREATE TRIGGER pracownik_delete ON Pracownik
INSTEAD OF DELETE AS 
IF(@@ROWCOUNT>1)
BEGIN
PRINT 'Nie mozna usunac wiecej niz jednego pracownika.'
ROLLBACK
END
ELSE
BEGIN
DECLARE @id INT
SET @id=(SELECT id_Pracownik from DELETED)
ROLLBACK
UPDATE Pracownik SET usuniety=1, dostepny=0 WHERE id_Pracownik=@id
PRINT 'Usunieto pracownika.'
END;
GO

--9b) Sprawdzenie, ¿e wyzwalacz 1 dzia³a
DELETE FROM Pracownik WHERE id_Pracownik=2 OR id_Pracownik=4;
DELETE FROM Pracownik WHERE id_Pracownik=5;
SELECT * FROM Pracownik;
GO

--10a) Tworzymy wyzwalacz 2
--Wyzwalacz "Lista_towar_insert" zmniejsza iloœæ towaru w magazynie 
--gdy towar zostaje dodany listy ³adunku pojazdu bior¹cego udzia³ w kursie.
--Sprawdza tak¿e, czy za³adowany towar nie przekracza ³adownoœci naczepy.
CREATE TRIGGER Lista_towar_insert ON Lista_towar
AFTER INSERT AS
DECLARE @id_pojazd INT
DECLARE @id_kurs INT
DECLARE @id_towar INT
DECLARE @ilosc INT
DECLARE inserted_towar_cursor CURSOR 
FOR SELECT id_Pojazd, id_Kurs, id_Towar, ilosc from INSERTED
OPEN inserted_towar_cursor
FETCH NEXT FROM inserted_towar_cursor INTO @id_pojazd, @id_kurs, @id_towar, @ilosc
WHILE (@@FETCH_STATUS=0)
BEGIN
IF((SELECT ladownosc FROM Naczepa where 
id_Naczepa=(SELECT id_Naczepa FROM Dane_kurs where id_Kurs=@id_kurs AND id_Pojazd=@id_pojazd))<
(SELECT 
(ilosc*
(SELECT typ.waga from Typ_towaru typ join Towar tow on tow.id_Typ_towaru=typ.id_Typ_towaru
where tow.id_Towar=@id_towar))
 FROM Lista_towar where id_Kurs=@id_kurs AND id_Pojazd=@id_pojazd) )
BEGIN
PRINT 'Waga ladunku przekracza ladownosc naczepy.'
ROLLBACK
END
ELSE
BEGIN
UPDATE Inwentarz_magazynu SET ilosc=ilosc-@ilosc where id_Towar=@id_towar
FETCH NEXT FROM inserted_towar_cursor INTO @id_pojazd, @id_kurs, @id_towar,@ilosc
END

END
CLOSE inserted_towar_cursor
DEALLOCATE inserted_towar_cursor;
GO

--10b) Sprawdzenie, ¿e wyzwalacz 2 dzia³a
INSERT INTO Lista_towar(id_Pojazd, id_Kurs, id_Towar, cena_netto, ilosc) VALUES(4,5,1, 1500, 2000);
INSERT INTO Lista_towar(id_Pojazd, id_Kurs, id_Towar, cena_netto, ilosc) VALUES(4,5,1, 1500, 50);
GO

--11a) Tworzymy wyzwalacz 3
--Wyzwalacz "zakoncz_kurs" uniemo¿liwia ukoñczenie wielu kursów jednoczeœnie i 
--zamiast usuwaæ informacje o kursie, zmienia wartoœæ atrybutu zakoñczony na 1
--i modyfikuje odpowiednio tabele pracownik, pojazd i naczepa ustawiaj¹c 
--atrybut dostepnosc na 1 i przyznaje dodatek dla kierowcy, który 
--zuzyje najmniej paliwa podczas kursu.
CREATE TRIGGER zakoncz_kurs ON Kurs
INSTEAD OF DELETE AS
IF(@@ROWCOUNT>1)
BEGIN
PRINT 'Nie mozna usuwac wielu kursow jednoczenie.'
ROLLBACK
END
ELSE
BEGIN
DECLARE @id_kurs INT
SET @id_kurs=(SELECT id_Kurs from DELETED)
UPDATE Pracownik SET dostepny=1 WHERE (id_Pracownik IN(SELECT id_Pracownik from Dane_kurs where id_Kurs=@id_kurs))
UPDATE Pojazd SET dostepny=1 WHERE (id_Pojazd IN(SELECT id_Pojazd from Dane_kurs where id_Kurs=@id_kurs))
UPDATE Naczepa SET dostepny=1 WHERE (id_Naczepa IN(SELECT id_Naczepa from Dane_kurs where id_Kurs=@id_kurs))
UPDATE Pracownik SET dodatek=dodatek+50 WHERE 
(id_Pracownik =(SELECT TOP 1 id_Kierowca from Dane_kurs where zuzyte_paliwo=
(SELECT TOP 1 zuzyte_paliwo from Dane_kurs
WHERE id_Kurs=@id_kurs
ORDER BY zuzyte_paliwo ASC)
 AND id_Kurs=@id_kurs))
UPDATE Kurs SET zakonczony=1 WHERE @id_kurs=id_Kurs

END;
GO

--11b) Sprawdzenie, ¿e wyzwalacz 3 dzia³a
DELETE Kurs where id_Kurs=5;
SELECT * from Kurs;
SELECT * from Pracownik;
GO

--12a) Tworzymy wyzwalacz 4
--Wyzwalacz "przeglad_check" sprawdza wa¿noœæ przegl¹du
-- pojazdów i naczep bior¹cych udzia³ w kursie.
CREATE TRIGGER przeglad_check on Dane_Kurs
AFTER INSERT AS
DECLARE @id_pojazd INT
DECLARE @id_naczepa INT
DECLARE przeglad_kurs CURSOR

FOR SELECT id_Pojazd, id_Naczepa FROM INSERTED
OPEN przeglad_kurs
FETCH NEXT FROM przeglad_kurs INTO @id_pojazd, @id_naczepa
WHILE (@@FETCH_STATUS=0)
BEGIN
	IF((SELECT data_wygasniecia_przegladu from Pojazd where id_Pojazd=@id_pojazd)<GETDATE())
	BEGIN
	PRINT 'Pojazd o ID: '+(CAST(@id_pojazd AS varchar))+' nie posiada wa¿nego przegl¹du.'
	ROLLBACK
	END
	IF((SELECT data_wygasniecia_przegladu from Naczepa where id_Naczepa=@id_naczepa)<GETDATE())
	BEGIN
	PRINT 'Naczepa o ID: '+(CAST(@id_pojazd AS varchar))+' nie posiada wa¿nego przegl¹du.'
	ROLLBACK
	END
FETCH NEXT FROM przeglad_kurs INTO @id_pojazd, @id_naczepa
END
CLOSE przeglad_kurs
DEALLOCATE przeglad_kurs;
GO

--12b) Sprawdzenie, ¿e wyzwalacz 4 dzia³a
INSERT INTO Dane_Kurs (id_Kurs, id_Pojazd, id_Kierowca, id_Naczepa, koszt, dystans, zuzyte_paliwo) VALUES (5, 6, 1, 4, 1467.11, 776, 93);
GO


--13a) Tworzymy tabelê przestawn¹
--Tabela przedstawia iloœæ i rodzaje zmagazynowanych towarów.
SELECT DISTINCT opis, [1] as 'Blacha stalowa 1m x 1m', [2] as 'Beczka Azotanu amonu',
[3] as 'Beczka Bromowodoru', [4] as 'Telewizor LG L540',[5] as  'Surówka hutnicza'
FROM 
(
SELECT i.id_Magazyn, i.ilosc, i.id_Towar, m.opis from Inwentarz_magazynu i left join Magazyn m
on i.id_Magazyn=m.id_Magazyn
)p
PIVOT
(
SUM(ilosc)
FOR [id_Towar] in ([1],[2],[3], [4], [5])
)
AS pvt;

--13b) Sprawdzenie, ¿e tabela przestawna dzia³a
SELECT DISTINCT opis, [1] as 'Blacha stalowa 1m x 1m', [2] as 'Beczka Azotanu amonu',
[3] as 'Beczka Bromowodoru', [4] as 'Telewizor LG L540',[5] as  'Surówka hutnicza'
FROM 
(
SELECT i.id_Magazyn, i.ilosc, i.id_Towar, m.opis from Inwentarz_magazynu i left join Magazyn m
on i.id_Magazyn=m.id_Magazyn
)p
PIVOT
(
SUM(ilosc)
FOR [id_Towar] in ([1],[2],[3], [4], [5])
)
AS pvt;