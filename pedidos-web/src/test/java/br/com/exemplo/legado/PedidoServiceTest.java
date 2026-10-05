package br.com.exemplo.legado;
import java.math.BigDecimal;
import org.junit.Test;
public class PedidoServiceTest {
 @Test public void aceitaPedidoValido() { PedidoService.validar("Ana", new BigDecimal("42.50")); }
 @Test(expected=IllegalArgumentException.class) public void rejeitaClienteVazio() { PedidoService.validar(" ", BigDecimal.ONE); }
 @Test(expected=IllegalArgumentException.class) public void rejeitaValorNegativo() { PedidoService.validar("Ana", new BigDecimal("-1")); }
 @Test(expected=IllegalArgumentException.class) public void rejeitaFracaoDeCentavo() { PedidoService.validar("Ana", new BigDecimal("1.001")); }
 @Test(expected=IllegalArgumentException.class) public void rejeitaOverflowDaColuna() { PedidoService.validar("Ana", new BigDecimal("10000000000")); }
}
