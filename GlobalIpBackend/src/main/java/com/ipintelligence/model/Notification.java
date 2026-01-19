package com.ipintelligence.model;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data

@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // FK user_id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // FK ip_asset_id
    @ManyToOne
    @JoinColumn(name = "ip_asset_id")
    private IpAsset ipAsset;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String type;

    private LocalDateTime timestamp;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public IpAsset getIpAsset() {
		return ipAsset;
	}

	public void setIpAsset(IpAsset ipAsset) {
		this.ipAsset = ipAsset;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public Notification(Integer id, User user, IpAsset ipAsset, String message, String type, LocalDateTime timestamp) {
		super();
		this.id = id;
		this.user = user;
		this.ipAsset = ipAsset;
		this.message = message;
		this.type = type;
		this.timestamp = timestamp;
	}

	public Notification() {
		super();
		// TODO Auto-generated constructor stub
	}
    
    
    
    
}

