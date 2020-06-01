import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class ATM {

	private Map<Integer, Integer> billetes;
    private List<Cuenta> listaDeCuentas;
    private Transaccion transacciones;
    private Cuenta cuentaActual;

    public ATM() {
    	billetes = new TreeMap<Integer, Integer>();
    	billetes.put(100, 500);
    	billetes.put(500, 500);
		billetes.put(1000, 500);		
    }

	public ATM(Map billetes, List<Cuenta> listaDeCuentas, Transaccion transacciones) {
        this.billetes = billetes;
        this.listaDeCuentas = listaDeCuentas;
        this.transacciones = transacciones;
    }
	
	public int getBilletes(int key){
		return billetes.get(key);
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
        	//Ingresar tarjeta
            int numeroDeTarjeta = Integer.parseInt(in.readLine());
            Tarjeta tarjeta = null;

            for (int i = 0; i < listaDeTarjetas.size(); i++) {
            	//Si el numero ingresado es lo mismo que el numero de la lista de tarjetas.
            	//La tarjeta existe.
                if (numeroDeTarjeta == listaDeTarjetas.get(i).getNumeroDeTarjeta()) {
                    tarjetaExiste = true;
                    tarjeta = listaDeTarjetas.get(i);
                    cuitActual = buscarCuitPorTarjeta(numeroDeTarjeta);
                    aliasActual = saberSuAliasAtravesDeCuit(cuitActual);
                    cuentaActual = listaDeCuentas.get(i);
                    break;
                }
            }

            //Si la tarjeta existe: ingresa el pin.
            if (tarjetaExiste) {
                System.out.println("Tarjeta aprobada");
                System.out.print("Ingresar pin: ");

                try {
                    int pin = Integer.parseInt(in.readLine());

                    for (int i = 0; i < listaDeTarjetas.size(); i++) {
                    	//Si el numero de pin ingresado es lo mismo que el numero de pin de la lista de tarjetas.
                    	//El pin existe.
                        if (pin == tarjeta.getPin()) {
                            pinExiste = true;
                            break;
                        }
                    }
                    
                    //Si el pin existe: elige una opcion.
                    if (pinExiste) {
                        System.out.println("Acceso aprobado");
                        System.out.println("Cuit: " + cuitActual);
                        System.out.println("Alias: " + aliasActual);
						System.out.println("Cuenta: " + cuentaActual);
						System.out.println("Saldo: " + cuentaActual.consultarSaldo());
                        elegirOpcion();
                    } else {
                        System.err.println("El pin ingresado no existe. Vuelve a intentarlo.");
                        leerTarjeta();
                    }

                } catch (Exception excepcion) {
                	System.err.println("No ingreso correctamente el pin");
                    leerTarjeta();
                }

            }else {
            	System.err.println("La tarjeta que ingreso no existe. Vuelve a intentarlo.");
            	leerTarjeta();
            }
            
            
            
        } catch (Exception exception) {
            System.err.println("No ingreso correctamente la tarjeta");
            leerTarjeta();
        }
    }

    //La lista de cuenta agrega el cliente.
    private void actualizarClientesDeCuentas(List<Cuenta> listaDeCuentas, List<Cliente> listaDeClientes) {
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

    //Recorre la lista de cuentas y devuelve un cuit (usando como parametro un numero de tarjeta).
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

    //Se le agrega el cliente un numero de tarjeta.
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
                    List<Tarjeta> tarjetasCliente = new LinkedList<>();
                    if (cuit.equals(Long.toString(cliente.getCuit()))) {
                        Tarjeta tarjeta = new Tarjeta(nroTarjeta, pin);
                        tarjetasCliente.add(tarjeta);
                        cliente.setTarjetas(tarjetasCliente);
                        //System.out.println("Agregue la tarjeta: " + tarjeta.getNumeroDeTarjeta() + " al cliente cuit: " + cliente.getCuit());
                    }
                }

                oneLine = lector.readLine();
            }
            lector.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("No se encontro archivo 'validacionDeTarjetas.txt'");
            System.exit(0);
        }
        return clientes;
    }

    //En la lista de cuentas se le agrega un cuit y un cliente.
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
            System.err.println("No se encontro archivo 'validacionDeTarjetas.txt'");
            System.exit(0);
        }
        return listaDeCuentas;
    }

    //Devuelve un string con su alias usando como parametro el numero de cuit.
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

    
    //Devuelve una lista de tarjetas.
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
            System.err.println("No se encontro archivo 'validacionDeTarjetas.txt'");
            System.exit(0);
        }

        return listaDeTarjetas;
    }

    //Devuelve una lista de cuit del cliente.
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

  //Devuelve una lista de alis del cliente.
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

    //Devuelve una lista de cuentas y se le agrega a la cuenta el saldo y su alias.
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

    //Elige la opcion.
    private void elegirOpcion() {

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("\nOpciones \n1- Retirar Efectivo\n2- Comprar Dolares\n3- Vender Dolares\n4- Depositar\n5- Transferir\n6- Salir");
        System.out.println("\nElija una opcion: ");

        try {
            int eleccion = Integer.parseInt(in.readLine());
            
            switch (eleccion) {
                case 1: {
                	retirarEfectivo();
                    break;
                }
                case 2: {
                    System.out.println("Comprar Dolares");
                    Cuenta cuenta = cuentaActual;  
                    Cuenta cuentaEnPesos = null;
                    CajaDeAhorroEnPesos c = null;
                    System.out.println("\nIngrese alias:");
                    String alias = in.readLine();
                    boolean existe = false;
                    for(int i = 0; i < listaDeCuentas.size();i++){
                    	if(alias.equals(listaDeCuentas.get(i).getAlias())){
                    		cuentaEnPesos = listaDeCuentas.get(i);
                    		if(cuentaEnPesos instanceof CajaDeAhorroEnPesos){
                    			existe = true;
                    			c = new CajaDeAhorroEnPesos();
                    			c.setSaldo(cuentaEnPesos.consultarSaldo());
                    		}
                    		break;
                    	}else {
                    		System.err.println("\nNo se encontro alias");
                         	elegirOpcion();
                    	}
                    }
                    
                    if(existe){
                   	 System.out.println("Encontro alias"); 
                    } else {
                		System.err.println("\nNo se encontro alias");
                    	elegirOpcion();
               	}
                    
                    ComprarDolares cd = new ComprarDolares(cuentaActual, c);
                    System.out.println("\n¿Cuanto desea comprar?");
                    double cantAComprar = Double.parseDouble(in.readLine());
                    cd.comprarDolares(BigDecimal.valueOf(cantAComprar));
                    System.out.println("\nSueldo cuenta actual: " + cuenta.consultarSaldo());
                    System.out.println("Sueldo en caja de ahorro en peso: " + c.consultarSaldo());
                    System.out.println(imprimirTicket("Comprar Dolares", BigDecimal.valueOf(cantAComprar)));
                    elegirOpcion();
                    break;
                }
                case 3: {
                	 System.out.println("Vender Dolares");
                     Cuenta cuenta = cuentaActual;  
                     Cuenta cuentaEnPesos = null;
                     CajaDeAhorroEnPesos c = null;
                     System.out.println("\nIngrese alias:");
                     String alias = in.readLine();	
                     boolean existe = false;
                     for(int i = 0; i < listaDeCuentas.size();i++){
                     	if(alias.equals(listaDeCuentas.get(i).getAlias())){
                     		cuentaEnPesos = listaDeCuentas.get(i);
                     		if(cuentaEnPesos instanceof CajaDeAhorroEnPesos){
                     			existe = true;
                     			c = new CajaDeAhorroEnPesos();
                     			c.setSaldo(cuentaEnPesos.consultarSaldo());
                     		}
                     		break;
                     	}
                     }    
                     
                     if(existe){
                    	 System.out.println("Encontro alias"); 
                     } else {
                 		System.err.println("\nNo se encontro alias");
                     	elegirOpcion();
                	}

                     VenderDolares vd = new VenderDolares(cuentaActual, c);
                     System.out.println("\n¿Cuanto desea vender?");
                     double cantAComprar = Double.parseDouble(in.readLine());
                     vd.venderDolares(BigDecimal.valueOf(cantAComprar));
                     System.out.println("\nSueldo cuenta actual: " + cuenta.consultarSaldo());
                     System.out.println("Sueldo en caja de ahorro en peso: " + c.consultarSaldo());
                     System.out.println(imprimirTicket("Vender Dolares", BigDecimal.valueOf(cantAComprar)));
                     elegirOpcion();
                     break;
                }
                case 4: {
                	depositar();
                    break;
                }
                case 5: {
                    System.out.println("Transferir");
                    Cuenta cuenta1 = cuentaActual;
                    Cuenta cuenta2 = null;
                    Transferencia t = new Transferencia(cuenta1);
                    System.out.println("\n¿Cuando desea transferir?");
                    double monto = Double.parseDouble(in.readLine());
                    System.out.println("\nIngrese alias: ");
                    String alias = in.readLine();
                    boolean existe = false;
                    for(int i = 0; i < listaDeCuentas.size();i++){
                    	if(alias.equals(listaDeCuentas.get(i).getAlias())){
                    		cuenta2 = listaDeCuentas.get(i);
                    		existe = true;
                    	} 
                    }
                    
                    if(existe){
                    	System.out.println("Encontro alias");
                    }else {
                		System.err.println("No se encontro alias");
                     	elegirOpcion();
                	}
                    
                    t.transferencia(BigDecimal.valueOf(monto), cuenta2);
                    System.out.println("\nSueldo de cuenta 1: " + cuenta1.consultarSaldo());
                    System.out.println("Sueldo de cuenta 2: " + cuenta2.consultarSaldo());
                    System.out.println(imprimirTicket("Transferir", BigDecimal.valueOf(monto)));
                    elegirOpcion();
                    break;
                } 
                case 6: {
                    System.out.println("\nAdios");
                    System.exit(0);                    
                    break;
                }
                default:
                    System.err.println("Seleccione una opcion");
                    elegirOpcion();
            }

        } catch (NumberFormatException e) {
            // TODO Bloque catch generado automĂ¡ticamente
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Bloque catch generado automĂ¡ticamente
            e.printStackTrace();
        }
    }
    
    private void retirarEfectivo(){
    	try {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			if(cuentaActual instanceof CajaDeAhorroEnDolares){
				throw new Error("No se puede extraer en dolares");
			}
			
			System.out.println("Retirar Efectivo");

			try {
				System.out.println("\n¿Cuanto desea retirar?");
				int dinero = Integer.parseInt(in.readLine());	
				Cuenta cuenta = cuentaActual; 
				Transaccion t = new RetirarEfectivo(cuenta);
				System.out.println("Billetes de 100: " + billetes.get(100));
				System.out.println("Billetes de 500: " + billetes.get(500));
				System.out.println("Billetes de 1000: " + billetes.get(1000));
				((RetirarEfectivo) t).retirarEfectivo(BigDecimal.valueOf(dinero));
				calcularBilletes(dinero, billetes);
				System.out.println("Billetes de 100: " + billetes.get(100));
				System.out.println("Billetes de 500: " + billetes.get(500));
				System.out.println("Billetes de 1000: " + billetes.get(1000));
				System.out.println(imprimirTicket("Retirar Efectivo", BigDecimal.valueOf(dinero)));

				
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
		
			elegirOpcion();
		} catch (NumberFormatException e) {
			// TODO Bloque catch generado automáticamente
			e.printStackTrace();
		} catch (Error e) {
			// TODO Bloque catch generado automáticamente
			e.printStackTrace();
		}
    }

    private void depositar(){
    	 try {
    		 BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			 System.out.println("Depositar");
			 System.out.println("\n¿Cuanto desea depositar?");

			 try {
				 	int dinero = Integer.parseInt(in.readLine());
					Cuenta cuenta = cuentaActual;
					Depositar d = new Depositar(cuenta);
					d.depositarPesos(BigDecimal.valueOf(dinero));

					System.out.println(imprimirTicket("Depositar", BigDecimal.valueOf(dinero)));
					
				} catch (NumberFormatException e) {
					// TODO Bloque catch generado automáticamente
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Bloque catch generado automáticamente
					e.printStackTrace();
				}
			
			 
			 elegirOpcion();
		} catch (NumberFormatException e) {
			// TODO Bloque catch generado automáticamente
			e.printStackTrace();
		} 
    }

    
    //Crea una cuenta.
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
 
    private int buscarCuit(List<Cliente> listaCliente, long cuit){
    	listaCliente = new LinkedList<>();
		  int n = listaCliente.size();
		  int centro = 0; 
		  int inf = 0;
		  int sup = n-1;
		   while(inf<=sup){
		     centro=(sup+inf)/2;
		     if(listaCliente.get(centro).getCuit() == cuit) return centro;
		     else if(cuit < listaCliente.get(centro).getCuit()){
		        sup=centro-1;
		     }
		     else {
		       inf=centro+1;
		     }
		   }
		   return -1;		   
	}
    
    private void calcularBilletes(int valorDeDinero, Map billetes){
    	if(valorDeDinero > 50000)
        {
          System.out.println("Limite de ATM.");
        }
        else if(valorDeDinero<=1000){
        	System.out.println(valorDeDinero/1000+" Thousands");
        }else
        {
          if(valorDeDinero<500)
          {
            System.out.println(valorDeDinero/100+" Hundreds");
          }
          else
          {
        	int t = 1;
            int h=5;
            int f=(valorDeDinero)/500;
            //System.out.println(n-500+" "+(n-500)/500+" "+(n-500)%500);
            t += ((valorDeDinero)%100)/100;
            h += ((valorDeDinero)%500)/100;
            if(h>5)
            {
              f=f+1;
              h=h-5;
            }
            System.out.println(t + " 1000 " + f +" 500 "+ h +" 100");
            
            int cantidadCien = (int) billetes.get(100);
            int cantidadQunientos = (int) billetes.get(500);
            int cantidadMil = (int) billetes.get(1000);
            
            billetes.remove(100);
            billetes.put(100, cantidadCien - h);
            billetes.remove(500);
            billetes.put(500, cantidadQunientos - f);
            billetes.remove(1000);
            billetes.put(1000, cantidadMil - t);
            
          }
        }
        
        
    }
    
    //Imprime ticket
    public String imprimirTicket(String tipoDeTransaccion, BigDecimal importe) {
    	LocalDate fecha = LocalDate.now();
    	LocalDateTime tiempo = LocalDateTime.now();
    	int hora  = tiempo.getHour();
    	int minuto = tiempo.getMinute();
    	int segundo = tiempo.getSecond();
        return "\nFecha: " + fecha + " - Hora: " + hora + ":" + minuto + ":" + segundo + 
        		" - Cuenta: " + cuentaActual.getAlias() + " - TipoDeTransaccion: " +  tipoDeTransaccion +  " - Importe En La Transaccion: " + importe +
        		" - Nuevo Saldo: $" + cuentaActual.consultarSaldo();
    }

}