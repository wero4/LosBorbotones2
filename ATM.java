import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;

public class ATM {

	private int[] billetes;
	private List<Cuenta> listaDeCuentas;
	private Transaccion transacciones;
	private Cuenta cuentaActual;

	public ATM() {

		billetes = new int[3];
		for (int i = 0; i < billetes.length; i++) {
			billetes[i] = 500;
		}

	}

	public void leerTarjeta() {
		boolean tarjetaExiste = false, pinExiste = false;
		long cuitActual = 0;
		String aliasActual = "";
		List<Tarjeta> listaDeTarjetas = leerArchivoTarjetas();
		this.listaDeCuentas = buscarCuentas();
		this.listaDeCuentas = actualizarCuentas(this.listaDeCuentas);

		List<Cliente> listaDeClientes = listaDeCuitDeClientesEnElArchivo();
		listaDeClientes = actualizarClientes(listaDeClientes);
		actualizarClientesDeCuentas(this.listaDeCuentas, listaDeClientes);

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Ingresar numero de Tarjeta: ");

		try {
			// Ingresar tarjeta
			int numeroDeTarjeta = Integer.parseInt(in.readLine());
			Tarjeta tarjeta = null;

			for (int i = 0; i < listaDeTarjetas.size(); i++) {
				// Si el numero ingresado es lo mismo que el numero de la lista
				// de tarjetas.
				// La tarjeta existe.
				if (numeroDeTarjeta == listaDeTarjetas.get(i)
						.getNumeroDeTarjeta()) {
					tarjetaExiste = true;
					tarjeta = listaDeTarjetas.get(i);
					cuitActual = buscarCuitPorTarjeta(numeroDeTarjeta);
					aliasActual = saberSuAliasAtravesDeCuit(cuitActual);
					cuentaActual = listaDeCuentas.get(i);
					break;
				}
			}

			// Si la tarjeta existe: ingresa el pin.
			if (tarjetaExiste) {
				System.out.println("Tarjeta aprobada");
				System.out.print("Ingresar pin: ");

				try {
					int pin = Integer.parseInt(in.readLine());

					for (int i = 0; i < listaDeTarjetas.size(); i++) {
						// Si el numero de pin ingresado es lo mismo que el
						// numero de pin de la lista de tarjetas.
						// El pin existe.
						if (pin == tarjeta.getPin()) {
							pinExiste = true;
							break;
						}
					}

					// Si el pin existe: elige una opcion.
					if (pinExiste) {
						System.out.println("Acceso aprobado");
						System.out.println("Cuit: " + cuitActual);
						System.out.println("Alias: " + aliasActual);
						System.out.println("Cuenta: " + cuentaActual);
						System.out.println("Saldo: " + cuentaActual.getSaldo());
						elejirOpcion();
					} else {
						System.err
								.println("El pin ingresado no existe. Vuelve a intentarlo.");
						leerTarjeta();
					}

				} catch (Exception excepcion) {
					System.err.println("No ingreso correctamente el pin");
					leerTarjeta();
				}

			} else {
				System.err
						.println("La tarjeta que ingreso no existe. Vuelve a intentarlo.");
				leerTarjeta();
			}

		} catch (Exception exception) {
			System.err.println("No ingreso correctamente la tarjeta");
			leerTarjeta();
		}
	}

	// La lista de cuenta agrega el cliente.
	private void actualizarClientesDeCuentas(List<Cuenta> listaDeCuentas,
			List<Cliente> listaDeClientes) {
		for (int i = 0; i < listaDeCuentas.size(); i++) {
			Cuenta cuenta = listaDeCuentas.get(i);
			for (int j = 0; j < listaDeClientes.size(); j++) {
				Cliente cliente = listaDeClientes.get(j);
				if (cuenta.getCliente().getCuit() == cliente.getCuit()) {
					cuenta.setCliente(cliente);
				}
			}
		}
	}

	// Recorre la lista de cuentas y devuelve un cuit (usando como parametro un
	// numero de tarjeta).
	private long buscarCuitPorTarjeta(int numeroTarjeta) {
		long cuit = 0;
		for (int i = 0; i < this.listaDeCuentas.size(); i++) {
			Cuenta cuenta = listaDeCuentas.get(i);
			for (int j = 0; j < cuenta.getCliente().getTarjetas().size(); j++) {
				Tarjeta tarjeta = cuenta.getCliente().getTarjetas().get(j);
				if (tarjeta.getNumeroDeTarjeta() == numeroTarjeta) {
					cuit = cuenta.getCliente().getCuit();
				}
			}
		}

		return cuit;
	}

	// Se le agrega el cliente un numero de tarjeta.
	private List<Cliente> actualizarClientes(List<Cliente> clientes) {
		try {
			FileReader archivo = new FileReader("validacionDeTarjetas.txt");
			BufferedReader lector = new BufferedReader(archivo);
			String oneLine = lector.readLine();

			while (oneLine != null) {
				String[] datos = oneLine.split(",");
				int nroTarjeta = Integer.parseInt(datos[0]);
				int pin = Integer.parseInt(datos[1]);
				String cuit = datos[2];

				for (int i = 0; i < clientes.size(); i++) {
					Cliente cliente = clientes.get(i);
					LinkedList<Tarjeta> tarjetasCliente = new LinkedList<>();
					if (cuit.equals(Long.toString(cliente.getCuit()))) {
						Tarjeta tarjeta = new Tarjeta(nroTarjeta, pin);
						tarjetasCliente.add(tarjeta);
						cliente.setTarjetas(tarjetasCliente);
						// System.out.println("Agregue la tarjeta: " +
						// tarjeta.getNumeroDeTarjeta() + " al cliente cuit: " +
						// cliente.getCuit());
					}
				}

				oneLine = lector.readLine();
			}
			lector.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err
					.println("No se encontro archivo 'validacionDeTarjetas.txt'");
			System.exit(0);
		}
		return clientes;
	}

	// En la lista de cuentas se le agrega un cuit y un cliente.
	private List<Cuenta> actualizarCuentas(List<Cuenta> listaDeCuentas) {
		try {
			FileReader archivo = new FileReader("clientes.txt");
			BufferedReader lector = new BufferedReader(archivo);
			String oneLine = lector.readLine();

			while (oneLine != null) {
				String[] datos = oneLine.split(",");
				long cuit = Long.parseLong(datos[0]);
				String alias = datos[1];

				for (int i = 0; i < listaDeCuentas.size(); i++) {
					Cuenta cuenta = listaDeCuentas.get(i);
					if (cuenta.getAlias().equals(alias)) {
						Cliente cliente = new Cliente();
						cliente.setCuit(cuit);
						cuenta.setCliente(cliente);
					}
				}

				oneLine = lector.readLine();
			}
			lector.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err
					.println("No se encontro archivo 'validacionDeTarjetas.txt'");
			System.exit(0);
		}
		return listaDeCuentas;
	}

	// Devuelve un string con su alias usando como parametro el numero de cuit.
	private String saberSuAliasAtravesDeCuit(long cuit) {
		String alias = "";
		for (int i = 0; i < this.listaDeCuentas.size(); i++) {
			Cuenta cuenta = this.listaDeCuentas.get(i);
			if (cuenta.getCliente().getCuit() == cuit) {
				alias = listaDeAliasDeClientesEnElArchivo().get(i);
				break;
			}
		}
		return alias;
	}

	// Devuelve una lista de tarjetas.
	private List<Tarjeta> leerArchivoTarjetas() {
		List<Tarjeta> listaDeTarjetas = new LinkedList<>();

		try {
			FileReader archivo = new FileReader("validacionDeTarjetas.txt");
			BufferedReader lector = new BufferedReader(archivo);
			String oneLine = lector.readLine();

			while (oneLine != null) {
				String[] datos = oneLine.split(",");
				int numeroDeTarjeta = Integer.parseInt(datos[0]);
				int pin = Integer.parseInt(datos[1]);
				listaDeTarjetas.add(new Tarjeta(numeroDeTarjeta, pin));
				oneLine = lector.readLine();
			}

			lector.close();
		} catch (Exception e) {
			System.err
					.println("No se encontro archivo 'validacionDeTarjetas.txt'");
			System.exit(0);
		}

		return listaDeTarjetas;
	}

	// Devuelve una lista de cuit del cliente.
	private List<Cliente> listaDeCuitDeClientesEnElArchivo() {
		List<Cliente> clientes = new LinkedList<>();

		try {
			FileReader archivo = new FileReader("clientes.txt");
			BufferedReader lector = new BufferedReader(archivo);
			String oneLine = lector.readLine();

			while (oneLine != null) {
				Cliente cliente = new Cliente();
				String[] datos = oneLine.split(",");
				long cuit = Long.valueOf(datos[0]);
				cliente.setCuit(cuit);
				clientes.add(cliente);
				oneLine = lector.readLine();
			}

			lector.close();
		} catch (Exception e) {
			System.err.println("No se encontro archivo 'clientes.txt'");
			System.exit(0);
		}

		return clientes;
	}

	// Devuelve una lista de alis del cliente.
	private List<String> listaDeAliasDeClientesEnElArchivo() {

		List<String> listaDeClientes = new LinkedList<>();
		try {
			FileReader archivo = new FileReader("clientes.txt");
			BufferedReader lector = new BufferedReader(archivo);
			String oneLine = lector.readLine();

			while (oneLine != null) {

				String[] datos = oneLine.split(",");
				String alias = datos[1];

				listaDeClientes.add(alias);
				oneLine = lector.readLine();
			}

			if (lector != null) {
				lector.close();
			}

		} catch (Exception e) {
			System.err.println("No se encontro archivo 'clientes.txt'");
			System.exit(0);
		}
		return listaDeClientes;

	}

	// Devuelve una lista de cuentas y se le agrega a la cuenta el saldo y su
	// alias.
	private List<Cuenta> buscarCuentas() {
		List<Cuenta> listaDeCuentas = new LinkedList<>();
		try {
			FileReader archivo = new FileReader("cuentas.txt");
			BufferedReader lector = new BufferedReader(archivo);
			String oneLine = lector.readLine();

			while (oneLine != null) {
				String[] datos = oneLine.split(",");
				int tipoCuenta = Integer.valueOf(datos[0]);
				String alias = datos[1];
				BigDecimal saldo = BigDecimal.valueOf(Double.valueOf(datos[2]));
				Cuenta cuenta = crearCuenta(tipoCuenta);
				cuenta.setSaldo(saldo);
				cuenta.setAlias(alias);
				listaDeCuentas.add(cuenta);
				oneLine = lector.readLine();
			}

			lector.close();
		} catch (Exception e) {
			System.err.println("No se encontro archivo 'cuentas.txt'");
			System.exit(0);
		}
		return listaDeCuentas;
	}

	// Elige la opcion.
	private void elejirOpcion() {

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		if (cuentaActual instanceof CajaDeAhorroEnDolares) {
			System.out
					.println("\nOpciones \n1- Comprar Dolares\n2- Vender Dolares\n3- Depositar\n4- Consultar saldo\n5- Consultar movimiento\n6- Salir");
			System.out.println("\nElija una opcion: ");

			try {
				int eleccion = Integer.parseInt(in.readLine());

				switch (eleccion) {
				case 1: {
					System.out.println("Comprar Dolares");
					Cuenta cuenta = cuentaActual;
					Cuenta cuentaEnPesos = null;
					CajaDeAhorroEnPesos c = null;
					System.out.println("\nIngrese alias:");
					String alias = in.readLine();
					boolean existe = false;
					for (int i = 0; i < listaDeCuentas.size(); i++) {
						if (alias.equals(listaDeCuentas.get(i).getAlias())) {
							cuentaEnPesos = listaDeCuentas.get(i);
							if (cuentaEnPesos instanceof CajaDeAhorroEnPesos) {
								existe = true;
								c = new CajaDeAhorroEnPesos();
								c.setSaldo(cuentaEnPesos.getSaldo());
							}
							break;
						} else {
							System.err.println("\nNo se encontro alias");
							elejirOpcion();
						}
					}

					if (existe) {
						System.out.println("Encontro alias");
					} else {
						System.err.println("\nNo se encontro alias");
						elejirOpcion();
					}

					ComprarDolares cd = new ComprarDolares(cuentaActual, c);
					System.out.println("\n¿Cuanto desea comprar?");
					double cantAComprar = Double.parseDouble(in.readLine());
					cd.comprarDolares(BigDecimal.valueOf(cantAComprar));
					System.out.println("\nSueldo cuenta actual: "
							+ cuenta.getSaldo());
					System.out.println("Sueldo en caja de ahorro en peso: "
							+ c.getSaldo());
					System.out.println(imprimirTicket("Comprar Dolares",
							BigDecimal.valueOf(cantAComprar)));
					elejirOpcion();
					break;
				}
				case 2: {
					System.out.println("Vender Dolares");
					Cuenta cuenta = cuentaActual;
					Cuenta cuentaEnPesos = null;
					CajaDeAhorroEnPesos c = null;
					System.out.println("\nIngrese alias:");
					String alias = in.readLine();
					boolean existe = false;
					for (int i = 0; i < listaDeCuentas.size(); i++) {
						if (alias.equals(listaDeCuentas.get(i).getAlias())) {
							cuentaEnPesos = listaDeCuentas.get(i);
							if (cuentaEnPesos instanceof CajaDeAhorroEnPesos) {
								existe = true;
								c = new CajaDeAhorroEnPesos();
								c.setSaldo(cuentaEnPesos.getSaldo());
							}
							break;
						}
					}

					if (existe) {
						System.out.println("Encontro alias");
					} else {
						System.err.println("\nNo se encontro alias");
						elejirOpcion();
					}

					VenderDolares vd = new VenderDolares(cuentaActual, c);
					System.out.println("\n¿Cuanto desea vender?");
					double cantAComprar = Double.parseDouble(in.readLine());
					vd.venderDolares(BigDecimal.valueOf(cantAComprar));
					System.out.println("\nSueldo cuenta actual: "
							+ cuenta.getSaldo());
					System.out.println("Sueldo en caja de ahorro en peso: "
							+ c.getSaldo());
					System.out.println(imprimirTicket("Vender Dolares",
							BigDecimal.valueOf(cantAComprar)));
					elejirOpcion();
					break;
				}
				case 3: {
					depositar();
					break;
				}
				case 4: {
					System.out.println("Su saldo actual es de: $"
							+ cuentaActual.getSaldo());
					elejirOpcion();
					break;
				}
				case 5: {
					System.out.print("Movimiento: "
							+ cuentaActual.getMovimientos());
					elejirOpcion();
					break;
				}
				case 6: {
					System.out.println("\nAdios");
					System.exit(0);
					break;
				}
				default:
					System.err.println("Seleccione una opcion");
					elejirOpcion();
				}

			} catch (NumberFormatException e) {
				// TODO Bloque catch generado automÃ¡ticamente
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Bloque catch generado automÃ¡ticamente
				e.printStackTrace();
			}
		} else {
			System.out
					.println("\nOpciones \n1- Retirar Efectivo\n2- Depositar\n3- Transferir\n4- Consultar saldo\n5- Consultar movimiento\n6- Salir");
			System.out.println("\nElija una opcion: ");

			try {
				int eleccion = Integer.parseInt(in.readLine());

				switch (eleccion) {
				case 1: {
					retirarEfectivo();
					break;
				}
				case 2: {
					depositar();
					break;
				}
				case 3: {
					System.out.println("Transferir");
					Cuenta cuenta1 = cuentaActual;
					Cuenta cuenta2 = null;
					Transferencia t = new Transferencia(cuenta1);
					System.out.println("\n¿Cuanto desea transferir?");
					double monto = Double.parseDouble(in.readLine());
					System.out.println("\nIngrese alias: ");
					String alias = in.readLine();
					if (alias.equals(cuentaActual.getAlias())) {
						System.err
								.print("No se puede transferir a la misma cuenta con la que esta operando");
					} else {

						boolean existe = false;
						for (int i = 0; i < listaDeCuentas.size(); i++) {
							if (alias.equals(listaDeCuentas.get(i).getAlias())) {
								cuenta2 = listaDeCuentas.get(i);
								existe = true;
							}
						}

						if (existe) {
							System.out.println("Encontro alias");
						} else {
							System.err.println("No se encontro alias");
							elejirOpcion();
						}

						t.transferencia(BigDecimal.valueOf(monto), cuenta2);
						System.out.println("\nSueldo de cuenta 1: "
								+ cuenta1.getSaldo());
						System.out.println("Sueldo de cuenta 2: "
								+ cuenta2.getSaldo());
						System.out.println(imprimirTicket("Transferir",
								BigDecimal.valueOf(monto)));
						System.out
								.println("Revertir Transferencia?: \nsi - no");
						String decision = in.readLine();
						if (decision.equals("si")) {
							t.reversible();
							System.out.println("\nSueldo de cuenta 1: "
									+ cuenta1.getSaldo());
							System.out.println("Sueldo de cuenta 2: "
									+ cuenta2.getSaldo());
							System.out.println("Transferencia revertida");
						}
						elejirOpcion();
					}
					elejirOpcion();
					break;
				}
				case 4: {
					System.out.println("Su saldo actual es de: $"
							+ cuentaActual.getSaldo());
					elejirOpcion();
					break;
				}
				case 5: {
					System.out.print("Movimiento: "
							+ cuentaActual.getMovimientos());
					elejirOpcion();
					break;
				}
				case 6: {
					System.out.println("\nAdios");
					System.exit(0);
					break;
				}
				default:
					System.err.println("Seleccione una opcion");
					elejirOpcion();
				}

			} catch (NumberFormatException e) {
				// TODO Bloque catch generado automÃ¡ticamente
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Bloque catch generado automÃ¡ticamente
				e.printStackTrace();
			}
		}
	}

	private void retirarEfectivo() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
			boolean hayBilletes = false;
			if (cuentaActual instanceof CajaDeAhorroEnDolares) {
				throw new Error("No se puede extraer en dolares");
			}

			System.out.println("Retirar Efectivo");

			try {
				System.out.println("\n¿Cuanto desea retirar?");
				int dineroIngresado = Integer.parseInt(in.readLine());
				Cuenta cuenta = cuentaActual;
				Transaccion transaccion = new RetirarEfectivo(cuenta);
				hayBilletes = calcularBilletes(dineroIngresado, cuenta,
						hayBilletes);

				if (hayBilletes) {
					((RetirarEfectivo) transaccion).retirarEfectivo(BigDecimal
							.valueOf(dineroIngresado));
				}
				System.out.println(imprimirTicket("Retirar Efectivo",
						BigDecimal.valueOf(dineroIngresado)));

			} catch (NumberFormatException e) {
				// TODO Bloque catch generado automáticamente
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Bloque catch generado automáticamente
				e.printStackTrace();
			} catch (Error e) {
				// TODO Bloque catch generado automáticamente
				e.printStackTrace();
			}

			elejirOpcion();
		} catch (NumberFormatException e) {
			// TODO Bloque catch generado automáticamente
			e.printStackTrace();
		} catch (Error e) {
			// TODO Bloque catch generado automáticamente
			e.printStackTrace();
		}
	}

	private void depositar() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
			System.out.println("Depositar");
			System.out.println("\n¿Cuanto desea depositar?");

			try {
				int dinero = Integer.parseInt(in.readLine());
				Cuenta cuenta = cuentaActual;
				Depositar d = new Depositar(cuenta);
				d.depositarPesos(BigDecimal.valueOf(dinero));

				System.out.println(imprimirTicket("Depositar",
						BigDecimal.valueOf(dinero)));

			} catch (NumberFormatException e) {
				// TODO Bloque catch generado automáticamente
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Bloque catch generado automáticamente
				e.printStackTrace();
			}

			elejirOpcion();
		} catch (NumberFormatException e) {
			// TODO Bloque catch generado automáticamente
			e.printStackTrace();
		}
	}

	// Crea una cuenta.
	private Cuenta crearCuenta(int tipoCuenta) {
		Cuenta cuenta = null;
		switch (tipoCuenta) {
		case 1:
			cuenta = new CajaDeAhorroEnPesos();
			break;
		case 2:
			cuenta = new CuentaCorriente();
			break;
		case 3:
			cuenta = new CajaDeAhorroEnDolares();
			break;
		default:
			break;
		}
		return cuenta;
	}

	private boolean calcularBilletes(int valor, Cuenta cuenta,
			boolean hayBillete) {
		int calculo = 0;
		int suma = 0;
		hayBillete = false;
		BigDecimal d = cuentaActual.getSaldo();
		int saldoDeCuenta = d.intValue();
		int cant = 0;

		for (int i = 0; i < billetes.length; i++) {
			if (billetes[i] > 0) {
				if (i == 0) {
					calculo = valor / 1000;
					if (calculo >= billetes[i] && billetes[i + 1] > 0
							&& billetes[i + 2] > 0) {
						// Tomo 2 de 500 hasta que la suma de lo mismo que el
						// calculo
						suma = calculo * 1000;
						while (calculo != billetes[i]) {
							// sacar 2 billetes de 500
							billetes[i + 1] -= 2;
							billetes[i] += 1;

						}
						billetes[i] -= calculo;
						hayBillete = true;
					} else {
						suma = calculo * 1000;
						billetes[i] -= calculo;
						hayBillete = true;
					}

				} else if (i == 1 && suma != valor) {
					calculo = (valor % 1000) / 500;
					suma = calculo * 500;
					billetes[i] -= calculo;
					hayBillete = true;

				} else if (i == 2 && suma != valor) {
					calculo = ((valor % 1000) % 500) / 100;
					suma = calculo * 100;
					billetes[i] -= calculo;
					hayBillete = true;
				}
			}

			if (billetes[i] <= 0 && i == 0) {

				//System.out.println("No hay billete de 1000");
				if (billetes[i] > 0) {

					if (i == 1 && suma != valor) {
//						System.out
//								.println("No hay billete de 1000,pero hay de 500");
						hayBillete = true;
					} else if (i == 2 && suma != valor) {
						//System.out
						//		.println("No hay billete de 1000,pero hay de 500");
						hayBillete = true;
					}
				}

			}
			if (billetes[i] <= 0 && i == 1) {

				//System.out.println("No hay billete de 500");
				hayBillete = true;
			}
			if (billetes[i] <= 0 && i == 2) {

				//System.err.println("No hay mas billetes en el sistema");
				hayBillete = false;

			}
		}

		
		return hayBillete;

	}

	// Imprime ticket
	public String imprimirTicket(String tipoDeTransaccion, BigDecimal importe) {
		LocalDate fecha = LocalDate.now();
		LocalDateTime tiempo = LocalDateTime.now();
		int hora = tiempo.getHour();
		int minuto = tiempo.getMinute();
		int segundo = tiempo.getSecond();
		return "\nFecha: " + fecha + " - Hora: " + hora + ":" + minuto + ":"
				+ segundo + " - Cuenta: " + cuentaActual.getAlias()
				+ " - TipoDeTransaccion: " + tipoDeTransaccion
				+ " - Importe En La Transaccion: " + importe
				+ " - Nuevo Saldo: $" + cuentaActual.getSaldo();
	}

}
