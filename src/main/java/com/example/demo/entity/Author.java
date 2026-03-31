package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

import lombok.Data;

@Data
@Entity
@Table(name = "author")
public class Author {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "author_id")
	private Long id;

	@Column(name = "author_name")
	private String name;

	@OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
	private List<CommitEntity> commits;
}