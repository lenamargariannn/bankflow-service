package com.bankflow.model;

import com.bankflow.dto.TransactionResponse;
import com.bankflow.model.enums.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_records_id_seq")
    @SequenceGenerator(name = "transaction_records_id_seq", sequenceName = "transaction_records_id_seq", allocationSize = 1)
    private Long id;

    @NotNull(message = "Transaction type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20, updatable = false)
    private TransactionType type;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than 0")
    @Column(name = "amount", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transaction_from_account"), updatable = false)
    private Account fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id", foreignKey = @ForeignKey(name = "fk_transaction_to_account"), updatable = false)
    private Account toAccount;

    @NotNull(message = "Timestamp cannot be null")
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(name = "description", length = 500, updatable = false)
    private String description;

    @Column(name = "performed_by", length = 255, updatable = false)
    private String performedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Initialize timestamps before persisting the entity.
     */
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        createdAt = LocalDateTime.now();
    }

    public TransactionResponse mapToTransactionResponse() {
        return TransactionResponse.builder()
                .id(this.getId())
                .type(this.getType())
                .amount(this.getAmount())
                .fromAccountId(this.getFromAccount() != null ? this.getFromAccount().getId() : null)
                .toAccountId(this.getToAccount() != null ? this.getToAccount().getId() : null)
                .timestamp(this.getTimestamp())
                .description(this.getDescription())
                .createdAt(this.getCreatedAt())
                .build();
    }
}

