package com.alexnail.blockchain.models;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Block {

    private long index;

    private LocalDateTime timestamp;

    private long proof;

    private String previousHash;
}
