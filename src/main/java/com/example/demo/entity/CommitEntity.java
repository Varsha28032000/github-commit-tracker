package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "commit")
public class CommitEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "commit_id")
	private Long id;

	@Column(name = "commit_message")
	private String message;

	@Column(name = "author_name") 
	private String authorName;

	@ManyToOne
	@JoinColumn(name = "author_id")
	private Author author;
}