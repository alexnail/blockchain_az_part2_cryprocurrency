package com.alexnail.blockchain.controllers;

import java.util.Collection;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.alexnail.blockchain.models.Block;
import com.alexnail.blockchain.models.Transaction;
import com.alexnail.blockchain.services.BlockchainService;

@RestController
public class BlockchainController {

    private final BlockchainService blockchainService;

    public BlockchainController(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    @GetMapping("/mine_block")
    public Block mineBlock() {
        return blockchainService.mineBlock();
    }

    @GetMapping("/get_chain")
    public Collection<Block> getChain() {
        return blockchainService.getChain();
    }

    @GetMapping("/is_valid")
    public ResponseEntity<String> isValid() {
        if (blockchainService.isValid()) {
            return new ResponseEntity<>("All good. The Blockchain is valid.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Houston, we have a problem. The Blockchain is not valid.", HttpStatus.OK);
        }
    }

    @PostMapping("/add_transaction")
    public ResponseEntity<String> addTransaction(@RequestBody Transaction transaction) {
        long idx = blockchainService.addTransaction(transaction);
        return new ResponseEntity<>(String.format("This transaction will be added to Block %d", idx), HttpStatus.CREATED);
    }

    @PostMapping("/connect_node")
    public ResponseEntity<String> connectNode(@RequestBody List<String> nodes) {
        if (nodes.isEmpty())
            return new ResponseEntity<>("No node", HttpStatus.BAD_REQUEST);
        else {
            return new ResponseEntity<>(blockchainService.addNodes(nodes).toString(), HttpStatus.CREATED);
        }
    }

    @GetMapping("/replace_chain")
    public ResponseEntity<String> replaceChain() {
        boolean isChainReplaced = blockchainService.replaceChain();
        if (isChainReplaced)
            return new ResponseEntity<>("The nodes had different chains so the chain was replaced by the longest one.",  HttpStatus.OK);
        else
            return new ResponseEntity<>("All good. The chain is the largest one.",  HttpStatus.OK);
    }
}
