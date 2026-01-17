# CodeBrewCafe ☕  
*A Java Object-Oriented Programming Project*

**CodeBrewCafe** is an academic Java project designed to simulate a cafe management system using core **Object-Oriented Programming (OOP)** principles. The project demonstrates clean software design, modular structure, and file-based data persistence.

This repository contains the full implementation of the system inside the `CafeManagementSystem/` directory.

---

## Project Overview

The system manages essential cafe operations such as:
- User authentication and session tracking
- Menu handling
- Order placement and storage
- Receipt generation
- File-based data management

The primary goal of this project is to apply **OOP concepts** in a practical, real-world-inspired scenario.

---

## Object-Oriented Concepts Used

- **Encapsulation** – Data and logic organized within classes  
- **Abstraction** – Clear separation of responsibilities  
- **Inheritance** – Reuse of shared behaviors  
- **Polymorphism** – Flexible method implementations  
- **Separation of Concerns** – UI, models, and data handling are isolated  

---

## Repository Structure

```text
codebrewcafe/
├── README.md # Repository documentation
└── CafeManagementSystem/
├── src/
│ ├── database/ # File handling & persistence logic
│ ├── models/ # Core business entities
│ ├── ui/ # User interface logic
│ └── Main.java # Application entry point
│
├── files/ # Runtime data (TXT-based storage)
│ ├── users.txt
│ ├── menu.txt
│ ├── orders.txt
│ ├── receipts.txt
│ ├── login_activity.txt
│ └── session_log.txt
│
├── lib/
│ └── flatlaf-3.6.jar # External UI library
│
├── .gitignore
└── CafeManagementSystem.iml
```
---

## Technologies Used

- **Language:** Java  
- **JDK:** OpenJDK 23  
- **IDE:** IntelliJ IDEA  
- **UI Library:** FlatLaf 3.6  
- **Data Storage:** File-based (`.txt` files)  
- **Version Control:** Git & GitHub  

---

## How to Run the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/SalmanJubayeed/codebrewcafe.git
   
2. Open IntelliJ IDEA
   
3. Open the folder:
codebrewcafe/CafeManagementSystem

4. Ensure flatlaf-3.6.jar is added as a library

5. Run Main.java

---

## Learning Outcomes

Hands-on experience with Object-Oriented Programming

Designing modular and maintainable Java applications

Managing file-based persistence

Integrating third-party libraries

Structuring a real-world inspired software project

---

## Future Improvements

Database integration (MySQL / SQLite)

Role-based access (Admin / Staff)

Improved GUI design

Exception handling and input validation

Unit testing

---

## Author

Salman Jubayeed
B.Sc. in Computer Science & Engineering
Student | Learning-focused

---

## License

This project is developed for educational purposes.
