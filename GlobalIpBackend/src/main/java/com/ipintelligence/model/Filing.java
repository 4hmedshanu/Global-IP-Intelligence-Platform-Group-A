package com.ipintelligence.model;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "filings")
@Data

@Builder
public class Filing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // FK ip_asset_id
    @ManyToOne
    @JoinColumn(name = "ip_asset_id")
    private IpAsset ipAsset;

    private LocalDateTime date;
    private String status;

    @Column(columnDefinition = "TEXT")
    private String description;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public IpAsset getIpAsset() {
		return ipAsset;
	}

	public void setIpAsset(IpAsset ipAsset) {
		this.ipAsset = ipAsset;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Filing(Integer id, IpAsset ipAsset, LocalDateTime date, String status, String description) {
		super();
		this.id = id;
		this.ipAsset = ipAsset;
		this.date = date;
		this.status = status;
		this.description = description;
	}

	public Filing() {
		super();
		// TODO Auto-generated constructor stub
	}
    
    
    
}

