package com.impal.gabungyuk.portofolio.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import java.time.LocalDateTime;

@Data

@JsonPropertyOrder({"portfolioId", "idPengguna", "title", "description", "fileUrl", "image", "uploadDate"})
public class PortfolioResponse {
    private Integer portfolioId;
    private Integer idPengguna;
    private String title;
    private String description;
    private String fileUrl;
    private String image;
    
    @JsonFormat(pattern = "HH:mm:ss - dd/MM/yyyy", timezone = "Asia/Jakarta")
    private LocalDateTime uploadDate;
}