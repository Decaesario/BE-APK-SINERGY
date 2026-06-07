package com.impal.gabungyuk.portofolio.model.response;

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

    private LocalDateTime uploadDate;
}
