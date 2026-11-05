import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class Demo {
    static Evento e(int id, int p) {
        return new Evento(id, Instant.parse("2026-01-01T00:00:00Z"),
                          "Evento " + id, p);
    }
    static void verificar(String nombre, Consumer<Evento[]> ordenar,
                          Comparator<Evento> criterio, boolean estable) {
        Random rnd = new Random(2024);
        for (int caso = 0; caso < 250; caso++) {
            int n = caso < 5 ? caso : rnd.nextInt(150);
            Evento[] a = new Evento[n];
            for (int i = 0; i < n; i++) a[i] = e(rnd.nextInt(500), rnd.nextInt(10));
            if (caso % 3 == 0) Arrays.sort(a, criterio);
            if (caso % 3 == 1) Arrays.sort(a, criterio.reversed());
            Evento[] esperado = a.clone();
            Arrays.sort(esperado, criterio);
            ordenar.accept(a);
            for (int i = 0; i < n; i++) {
                if (criterio.compare(a[i], esperado[i]) != 0)
                    throw new AssertionError(nombre + ": orden incorrecto");
                if (estable && a[i] != esperado[i])
                    throw new AssertionError(nombre + ": estabilidad");
            }
            Set<Evento> originales = Collections.newSetFromMap(new IdentityHashMap<>());
            originales.addAll(Arrays.asList(esperado));
            for (Evento item : a)
                if (!originales.remove(item)) throw new AssertionError("Permutacion");
            if (!originales.isEmpty()) throw new AssertionError("Perdida");
        }
        System.out.println(nombre + ": 250 casos OK");
    }
    public static void main(String[] args) {
        Comparator<Evento> c = Ordenamientos.POR_PRIORIDAD;
        verificar("Bubble", a -> Ordenamientos.bubble(a,c), c, true);
        verificar("Selection", a -> Ordenamientos.selection(a,c), c, false);
        verificar("Insertion", a -> Ordenamientos.insertion(a,c), c, true);
        verificar("Merge", a -> Ordenamientos.merge(a,c), c, true);
        verificar("Quick", a -> Ordenamientos.quick(a,c), c, false);
        verificar("Heap", a -> Ordenamientos.heap(a,c), c, false);
        verificar("Counting", Ordenamientos::counting, c, true);
        verificar("Radix", Ordenamientos::radix,
                  Comparator.comparingInt(Evento::getId), true);
        verificar("Bucket", Ordenamientos::bucket, c, true);
        verificar("Shell", a -> Ordenamientos.shell(a,c), c, false);
        Evento[] extremos = {e(Integer.MAX_VALUE,9), e(0,0), e(10,1)};
        Ordenamientos.radix(extremos);
        if (extremos[2].getId() != Integer.MAX_VALUE) throw new AssertionError();
        Evento[] todosIguales = new Evento[200];
        for (int i=0;i<200;i++) todosIguales[i]=e(i,3);
        Ordenamientos.quick(todosIguales,c);
        Evento[] ejemplo = {e(170,3),e(45,1),e(75,3),e(90,0),e(802,1),e(24,2)};
        Ordenamientos.counting(ejemplo);
        System.out.println(Arrays.toString(ejemplo));
        ejemplosPorFecha();
        medirTiempos(pedirCantidad(new Scanner(System.in)));
    }
    static int pedirCantidad(Scanner sc) {
        System.out.print("\nCantidad de eventos a generar (entero positivo): ");
        String s = sc.next();
        // [1-9]\d{0,8}: positivo y sin desbordar int
        return s.matches("[1-9]\\d{0,8}") ? Integer.parseInt(s) : pedirCantidad(sc);
    }
    static Evento[] generarMock(int n) {
        Random rnd = new Random(42);
        Instant base = Instant.parse("2020-01-01T00:00:00Z");
        return IntStream.range(0, n)
            .mapToObj(i -> new Evento(rnd.nextInt(1_000_000),
                base.plusSeconds(rnd.nextInt(200_000_000)), "Mock " + i, rnd.nextInt(10)))
            .toArray(Evento[]::new);
    }
    static void medirTiempos(int n) {
        Evento[] datos = generarMock(n);
        Comparator<Evento> f = Ordenamientos.POR_FECHA;
        System.out.println("\nTiempos con " + n + " eventos:");
        List.<Map.Entry<String, Consumer<Evento[]>>>of(
            Map.entry("Bubble (fecha)", a -> Ordenamientos.bubble(a, f)),
            Map.entry("Selection (fecha)", a -> Ordenamientos.selection(a, f)),
            Map.entry("Insertion (fecha)", a -> Ordenamientos.insertion(a, f)),
            Map.entry("Merge (fecha)", a -> Ordenamientos.merge(a, f)),
            Map.entry("Quick (fecha)", a -> Ordenamientos.quick(a, f)),
            Map.entry("Heap (fecha)", a -> Ordenamientos.heap(a, f)),
            Map.entry("Shell (fecha)", a -> Ordenamientos.shell(a, f)),
            // estos tres no reciben comparador: ordenan por su clave fija
            Map.entry("Counting (prioridad)", Ordenamientos::counting),
            Map.entry("Radix (id)", Ordenamientos::radix),
            Map.entry("Bucket (prioridad)", Ordenamientos::bucket)
        ).forEach(e -> {
            Evento[] a = datos.clone();
            long inicio = System.nanoTime();
            e.getValue().accept(a);
            System.out.printf("  %-22s %10.3f ms%n", e.getKey(), (System.nanoTime() - inicio) / 1e6);
        });
    }
    static void imprimir(String titulo, Evento[] a) {
        System.out.println("\n" + titulo);
        Arrays.stream(a).forEach(x -> System.out.println(
            "  " + x.getFechaCreacion() + "  #" + x.getId() + " " + x.getDescripcion()));
    }
    static void ejemplosPorFecha() {
        Evento[] eventos = {
            new Evento(1, Instant.parse("2026-03-15T09:30:00Z"), "Reunion de equipo", 2),
            new Evento(2, Instant.parse("2025-12-01T14:00:00Z"), "Cierre contable", 1),
            new Evento(3, Instant.parse("2026-07-20T08:00:00Z"), "Auditoria", 0),
            new Evento(4, Instant.parse("2026-01-10T17:45:00Z"), "Capacitacion", 3),
            new Evento(5, Instant.parse("2025-11-28T11:15:00Z"), "Entrega proyecto", 1),
            new Evento(6, Instant.parse("2026-03-15T08:00:00Z"), "Desayuno", 4),
        };
        imprimir("Original", eventos);

        Evento[] porMerge = eventos.clone();
        Ordenamientos.merge(porMerge, Ordenamientos.POR_FECHA);
        imprimir("1) Merge sort por fecha (mas antiguo primero)", porMerge);

        Evento[] porQuick = eventos.clone();
        Ordenamientos.quick(porQuick, Ordenamientos.POR_FECHA.reversed());
        imprimir("2) Quick sort por fecha (mas reciente primero)", porQuick);

        Evento[] porHeap = eventos.clone();
        Ordenamientos.heap(porHeap, Ordenamientos.POR_FECHA);
        imprimir("3) Heap sort por fecha (mas antiguo primero)", porHeap);
    }
}
