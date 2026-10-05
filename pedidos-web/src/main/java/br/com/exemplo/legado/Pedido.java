package br.com.exemplo.legado;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.*;
@Entity @Table(name="PEDIDO")
public class Pedido implements Serializable {
 private static final long serialVersionUID = 1L;
 @Id @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="pedidoSeq")
 @SequenceGenerator(name="pedidoSeq", sequenceName="PEDIDO_SEQ", allocationSize=1)
 private Long id;
 @Column(nullable=false, length=120) private String cliente;
 @Column(nullable=false, precision=12, scale=2) private BigDecimal valor;
 protected Pedido() { }
 public Pedido(String cliente, BigDecimal valor) { this.cliente=cliente; this.valor=valor; }
 public Long getId() { return id; }
 public String getCliente() { return cliente; }
 public BigDecimal getValor() { return valor; }
}
