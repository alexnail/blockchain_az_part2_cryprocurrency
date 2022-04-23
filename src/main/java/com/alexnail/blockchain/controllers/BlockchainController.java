package com.alexnail.blockchain.controllers;

import java.util.Collection;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alexnail.blockchain.models.Block;
import com.alexnail.blockchain.services.BlockchainService;

@RestController
public class BlockchainController {

    private final BlockchainService blockchainService;

    public BlockchainController(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    @GetMapping("/mine_block")
    Block mineBlock() {
        return blockchainService.mineBlock();
    }

    @GetMapping("/get_chain")
    Collection<Block> getChain() {
        return blockchainService.getChain();
    }

    @GetMapping("/is_valid")
    ResponseEntity<String> isValid() {
        if (blockchainService.isValid()) {
            return new ResponseEntity<>("All good. The Blockchain is valid.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Houston, we have a problem. The Blockchain is not valid.", HttpStatus.OK);
        }
    }
}
