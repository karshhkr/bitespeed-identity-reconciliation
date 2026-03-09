# Bitespeed Identity Reconciliation API

This project implements the Identity Reconciliation backend assignment for Bitespeed.

The service exposes an API that identifies and links customer contacts based on email and phone number.  
It ensures that related contacts are grouped under a single primary contact while maintaining a list of secondary contacts.

---

## Tech Stack

- Java
- Spring Boot
- Spring Data JPA
- Maven
- H2 / MySQL
- REST API

---

## API Endpoint

POST /identify

Example:http://localhost:8080/identify
