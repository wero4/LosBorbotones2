
import java.awt.List;
import java.util.LinkedList;

public class Cliente {

	private LinkedList<Tarjeta> tarjetas;
	private long cuit;
	
	public Cliente(LinkedList<Tarjeta> tarjetas, long cuit) {
		if(cuit < 99999999999L || cuit < 11111111111L) {
			setCuit(cuit);
			tarjetas = new LinkedList<Tarjeta>();	
		}
	}
	
	public Cliente() {
			
	}
	
	public void agregarTarjetas(Tarjeta tarjeta) {
		
		tarjetas.add(tarjeta);
	}

	public LinkedList<Tarjeta> getTarjetas() {
		return tarjetas;
	}

	public void setTarjetas(LinkedList<Tarjeta> tarjetas) {
		this.tarjetas = tarjetas;
	}

	public long getCuit() {
		return cuit;
	}

	public void setCuit(long cuit) {
		if (contarDigitos(cuit) == 11) {
			this.cuit = cuit;
		} else
			System.out.println("Error CUIT invalido");
	}
	
	private int contarDigitos(long num) {
		int contador = 0;
		long numero = num;
		while (numero != 0) {
			numero /= 10;
			contador++;
		}

		return contador;
	}
}
