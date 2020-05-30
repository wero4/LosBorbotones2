

import java.math.BigDecimal;

public class CajaDeAhorroEnPesos extends Cuenta {

	public CajaDeAhorroEnPesos(Cliente cliente, String alias) {
		super(cliente, alias);
	}

	public CajaDeAhorroEnPesos() {
		// TODO Apéndice de constructor generado automáticamente
	}

	@Override
	public void ingresarEfectivo(BigDecimal montoIngresado) {
		setSaldo(consultarSaldo().add(montoIngresado));

	}

	@Override
	public void descontarEfectivo(BigDecimal montoDescontado) {
		if (!haySaldo(montoDescontado)) {
			setSaldo(consultarSaldo().subtract(montoDescontado));
		}

	}

	public boolean haySaldo(BigDecimal montoDescontado) {
		return consultarSaldo().compareTo(montoDescontado) < 0 ;
	}

}
