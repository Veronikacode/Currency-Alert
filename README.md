# Alarm Walutowy

System "Alarm Walutowy" składa się z dwóch mikroserwisów: **DataGatherer** odpowiedzialnego za pobieranie kursów walut z zewnętrznego API oraz **DataProvider** oferującego REST API do zarządzania subskrypcjami i historią kursów. Projekt realizuje asynchroniczną komunikację poprzez RabbitMQ oraz wykorzystuje Flyway do zarządzania schematami baz danych.
