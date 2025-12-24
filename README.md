# just_store_anything
Java version of DvorakDwarf's  Infinite-Storage-Glitch with frontned


## Core Flow
```mermaid
sequenceDiagram
    participant C as Client
    participant S as Server
    participant E as Encryption Module
    participant B as Binary Converter
    participant V as Frame Generator & Video Combiner
    participant Y as YouTube API
    participant DB as PostgreSQL

    Note over C,DB: Upload Flow
    C->>S: Send File + Secret Key + Metadata
    S->>E: Send File + Secret Key
    E->>S: Return Encrypted File
    S->>B: Send Encrypted File
    B->>V: Stream Binary String
    V->>S: Return Video File
    S->>Y: Upload Video
    Y->>S: Return YT Response Data
    S->>DB: Store YT Response + File Details
    S->>C: Return Metadata Confirmation

    Note over C,DB: Retrieval Flow
    C->>S: Request File (with Metadata/ID)
    S->>DB: Query Stored Data
    DB->>S: Return YT Video ID & Details
    S->>Y: Request Video
    Y->>S: Return Video File
    S->>B: Convert Video to Binary
    B->>S: Return Binary Stream
    S->>E: Decrypt with Secret Key
    E->>S: Return Original File
    S->>C: Send Original File
```


## What implemented

### Frontend
- [ ] YouTube access
- [ ] Main page (file upload, other data)
- [ ] Backend connectivity
- [ ] retriever page

### Backend
- [ ] Encryption module
- [ ] file to binary string
- [ ] BS to image
- [ ] images  to video
- [ ] user oauth from Google for YouTube
- [ ] YouTube upload
- [ ] Database config and storing
- [ ] retriever file base on metadata