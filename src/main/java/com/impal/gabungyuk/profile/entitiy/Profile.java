package com.impal.gabungyuk.profile.entitiy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
@Builder

public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name= "id_pengguna", nullable = false)
    private Integer idPengguna;

    @Column(name = "profile_picture", nullable = true)
    private String profilePicture;

    @Column(name = "nama_lengkap", nullable = false)
    private String namaLengkap;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "institusi", nullable = true)
    private String  institusi;

    @Column(name = "bio", nullable = true)
    private String bio;

    @Column(name = "keahlian", nullable = true)
    private String keahlian;

    @Column(name = "lokasi", nullable = true)
    private String lokasi;

    @Column(name = "whatsapp", nullable = true)
    private String whatsapp;

    @Column(name = "instagram", nullable = true)
    private String instagram;

    @Column(name = "facebook", nullable = true)
    private String facebook;

    @Column(name = "linkedin", nullable = true)
    private String linkedin;

    @Column(name = "timezone", nullable = true)
    private String timezone;
    
}
