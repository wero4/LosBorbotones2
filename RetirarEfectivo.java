
import java.math.BigDecimal;

public class RetirarEfectivo extends Transaccion {

	public RetirarEfectivo(Cuenta cuenta) {
		super(cuenta);
	}
	
	// saldo debe ser mayor o igual a monto
	public void retirarEfectivo(BigDecimal montoRetirado) {
			// A.compareTo(B): este metodo retorna -1 si A < B, 0 si A = B, 1 si
			// A > B
			if (haySaldo(montoRetirado)) {
				super.setMonto(montoRetirado);
				super.getCuenta().descontarEfectivo(montoRetirado);
				super.generarMovimiento();
				System.out.println("la extraccion se realizo con exito");

			} else {
				throw new Error("Saldo Insuficiente");
			}
		
	}

	private boolean haySaldo(BigDecimal montoRetirado) {
		if (super.getCuenta() instanceof CuentaCorriente) {
			return getCuenta().getSaldo().compareTo(montoRetirado) >= -1;

		} else {
			return getCuenta().getSaldo().compareTo(montoRetirado) >= 0;
		}
	}
}
