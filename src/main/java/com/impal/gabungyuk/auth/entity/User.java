package com.impal.gabungyuk.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pengguna")
    private Integer idPengguna;

    @Column(name = "nama_lengkap", nullable = false)
    private String namaLengkap;

    // @Column(name = "username", nullable = false, unique = true)
    // private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "firebase_uid", unique = true)
    private String firebaseUid;

    @Column(name = "provider")
    private String provider;

    @Column(name = "profile_picture", nullable = true)
    private String profilePicture;

    @Column(name = "institusi", nullable = true)
    private String institusi;

    @Column(name = "bio", nullable = true)
    private String bio;

    @Column(name = "keahlian", nullable = true)
    private String keahlian;

    @Column(name = "lokasi", nullable = true)
    private String lokasi;

    @Column(name = "whatsapp", nullable = true)
    private String whatsapp;
}
