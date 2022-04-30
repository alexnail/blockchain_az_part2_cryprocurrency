package com.alexnail.blockchain.services;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alexnail.blockchain.models.Block;
import com.alexnail.blockchain.models.Transaction;
import com.google.common.hash.Hashing;

@Service
public class BlockchainService {

    public static final String NODE_ADDRESS = UUID.randomUUID().toString();

    private List<Block> chain = new ArrayList<>();

    private final List<Transaction> transactions = new ArrayList<>();

    private final Set<String> nodes = new HashSet<>();

    public BlockchainService() {
        chain.add(createBlock(1, "0"));
    }

    public Block mineBlock() {
        Block previousBlock = getPreviousBlock();
        long previousProof = previousBlock.getProof();
        long proof = proofOfWork(previousProof);
        String previousHash = hash(previousBlock);
        addTransaction(new Transaction(NODE_ADDRESS, "Alex", BigDecimal.ONE));
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
        block.setTransactions(new ArrayList<>(transactions));
        transactions.clear();
        return block;
    }

    public Collection<Block> getChain() {
        return chain;
    }

    public boolean isValid() {
        return isValid(chain);
    }

    private boolean isValid(List<Block> chn) {
        Block previousBlock = chn.get(0);
        int blockIndex = 1;
        while (blockIndex < chn.size()) {
            Block block = chn.get(blockIndex);
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

    public long addTransaction(Transaction transaction) {
        transactions.add(transaction);
        return getPreviousBlock().getIndex() + 1;
    }

    public Set<String> addNodes(List<String> nodes) {
        this.nodes.addAll(nodes);
        return this.nodes.stream().collect(Collectors.toUnmodifiableSet());
    }

    public boolean replaceChain() {
        List<Block> longestChain = null;
        int maxLength = chain.size();

        for (String node: nodes) {
            // request the node for its chain
            RestTemplate restTemplate = new RestTemplate();
            String url = String.format("%s/get_chain", node);
            ResponseEntity<List<Block>> response = restTemplate.exchange(url, HttpMethod.GET, null,
                                                                         new ParameterizedTypeReference<>() {});
            if (response.getStatusCode().is2xxSuccessful()) {
                List<Block> nodeChain = response.getBody();
                if (nodeChain.size() > maxLength && isValid(nodeChain)) {
                    maxLength = nodeChain.size();
                    longestChain = nodeChain;
                }
            }
            if (Objects.nonNull(longestChain)) {
                this.chain = longestChain;
                return true;
            }
        }
        return false;
    }
}
