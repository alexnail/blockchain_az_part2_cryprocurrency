package com.alexnail.blockchain.services;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alexnail.blockchain.models.Block;
import com.google.common.hash.Hashing;

@Service
public class BlockchainService {

    private final List<Block> chain = new ArrayList<>();

    public BlockchainService() {
        chain.add(createBlock(1, "0"));
    }

    public Block mineBlock() {
        Block previousBlock = getPreviousBlock();
        long previousProof = previousBlock.getProof();
        long proof = proofOfWork(previousProof);
        String previousHash = hash(previousBlock);
        Block block = createBlock(proof, previousHash);
        chain.add(block);
        return block;
    }

    private String hash(Block block) {
        return Hashing.sha256().hashBytes(block.toString().getBytes(StandardCharsets.UTF_8)).toString();
    }

    private long proofOfWork(long previousProof) {
        long newProof = 1;
        boolean checkProof = false;
        while (!checkProof) {
            String hashOperation = Hashing.sha256().hashLong((long) (Math.pow(newProof, 2) - Math.pow(previousProof, 2))).toString();
            if (hashOperation.startsWith("0000")) {
                checkProof = true;
            } else {
                newProof++;
            }
        }
        return newProof;
    }

    private Block getPreviousBlock() {
        return chain.get(chain.size() - 1);
    }

    private Block createBlock(long proof, String previousHash) {
        Block block = new Block();
        block.setIndex(chain.size() + 1);
        block.setTimestamp(LocalDateTime.now());
        block.setProof(proof);
        block.setPreviousHash(previousHash);
        return block;
    }

    public Collection<Block> getChain() {
        return chain;
    }

    public boolean isValid() {
        Block previousBlock = chain.get(0);
        int blockIndex = 1;
        while (blockIndex < chain.size()) {
            Block block = chain.get(blockIndex);
            if (!block.getPreviousHash().equals(hash(previousBlock)))
                return false;

            long previousProof = previousBlock.getProof();
            long proof = block.getProof();
            String hashOperation = Hashing.sha256().hashLong((long) (Math.pow(proof, 2) - Math.pow(previousProof, 2))).toString();
            if (!hashOperation.startsWith("0000"))
                return false;

            previousBlock = block;
            blockIndex++;
        }
        return true;
    }
}
