import java.io.Serializable;
import java.util.*;

public class TSB_OAHashtable<K, V> implements Map<K, V>, Cloneable, Serializable
{
    private final static int MAX_SIZE = Integer.MAX_VALUE;
    private ArrayList<Map.Entry<K, V>> table[];
    private int initial_capacity;
    private int count;
    private float load_factor;
    private transient Set<K> keySet = null;
    private transient Set<Map.Entry<K,V>> entrySet = null;
    private transient Collection<V> values = null;
    protected transient int modCount;

    //Constructores

    public TSB_OAHashtable()
    {
        this(11, 0.5f);
    }

    public TSB_OAHashtable(int initial_capacity)
    {
        this(initial_capacity, 0.5f);
    }

    public TSB_OAHashtable(int initial_capacity, float load_factor)
    {
        if(load_factor <= 0) { load_factor = 0.5f; }
        if(initial_capacity <= 0) { initial_capacity = 11; }
        else
        {
            if(!esPrimo(initial_capacity))
            {
                initial_capacity = siguientePrimo(initial_capacity);
            }
            if(initial_capacity > TSB_OAHashtable.MAX_SIZE)
            {
                initial_capacity = TSB_OAHashtable.MAX_SIZE;
            }
        }


        this.table = new ArrayList[initial_capacity];


        this.initial_capacity = initial_capacity;
        this.load_factor = load_factor;
        this.count = 0;
        this.modCount = 0;
    }


    public TSB_OAHashtable(Map<? extends K,? extends V> t)
    {
        this(11, 0.5f);
        this.putAll(t);
    }


    //Metodos especificados por Map

    public int size(){return this.count;}


    public boolean isEmpty() {return (this.count == 0);}


    public boolean containsKey(Object key) {return (this.get((K)key) != null);}


    public boolean containsValue(Object value)
    {
        return this.contains(value);
    }


    public V get(Object key)
    {
        if(key == null) throw new NullPointerException("get(): parámetro null");

        int i = this.h(key.hashCode());
        Map.Entry<K, V> map = (Map.Entry<K, V>) this.table[i];

        int j = 1;
        while (i <= TSB_OAHashtable.this.table.length)
        {
            if(key.equals(map.getKey())){ return map.getValue(); }
            else
            {
                map = (Map.Entry<K, V>) this.table[siguienteIndex(i,j)];
                j++;
            }
        }
        return null;
    }

    public V put(K key, V value)
    {
        if(key == null || value == null) throw new NullPointerException("put(): parámetro null");

        int i = this.h(key);
        Map.Entry<K, V> map = (Map.Entry<K, V>) this.table[i];

        V old = null;
        int j = 1;
        while (i <= TSB_OAHashtable.this.table.length && !key.equals(map.getKey()))
        {
                map = (Map.Entry<K, V>) this.table[siguienteIndex(i,j)];
                j++;
        }

        if(map != null)
        {
            old = map.getValue();
            map.setValue(value);
        }
        else
        {
            if(this.averageLength() >= this.load_factor * 10) this.rehash();
            i = this.h(key);
            j =1;

            Map.Entry<K, V> entry = new Entry<>(key, value);
            while (!this.table[i].isEmpty())
            {
                i = siguienteIndex(i,j);
            }
            this.table[i] = (ArrayList<Map.Entry<K, V>>) entry;
            this.count++;
            this.modCount++;
        }

        return old;
    }


    public V remove(Object key)
    {
        if(key == null) throw new NullPointerException("remove(): parámetro null");

        int i = this.h(key.hashCode());
        Map.Entry<K, V> map = (Map.Entry<K, V>) this.table[i];

        V old = null;
        int j = 1;

        while (i <= TSB_OAHashtable.this.table.length && !key.equals(map.getKey()))
        {
            map = (Map.Entry<K, V>) this.table[siguienteIndex(i,j)];
            j++;
        }
        if(!(i <= TSB_OAHashtable.this.table.length))
        {
            old = map.getValue();
            map.setValue(null);
            this.count--;
            this.modCount++;
        }

        return old;
    }


    public void putAll(Map<? extends K, ? extends V> m)
    {
        for(Map.Entry<? extends K, ? extends V> e : m.entrySet())
        {
            put(e.getKey(), e.getValue());
        }
    }


    public void clear()
    {
        this.table = new ArrayList[this.initial_capacity];
        this.count = 0;
        this.modCount++;
    }


    public Set<K> keySet()
    {
        if(keySet == null)
        {
            keySet = new KeySet();
        }
        return keySet;
    }

    public Collection<V> values()
    {
        if(values==null)
        {
            values = new ValueCollection();
        }
        return values;
    }


    public Set<Map.Entry<K, V>> entrySet()
    {
        if(entrySet == null)
        {
            entrySet = new EntrySet();
        }
        return entrySet;
    }

    //Redefinicion de los metodos heredados de Object

    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        TSB_OAHashtable<K, V> t = (TSB_OAHashtable<K, V>)super.clone();
        t.table = new ArrayList[table.length];
        for (int i = table.length ; i-- > 0 ; )
        {
            t.table[i] = (ArrayList<Map.Entry<K, V>>) table[i].clone();
        }
        t.keySet = null;
        t.entrySet = null;
        t.values = null;
        t.modCount = 0;
        return t;
    }


    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof Map)) { return false; }

        Map<K, V> t = (Map<K, V>) obj;
        if(t.size() != this.size()) { return false; }

        try
        {
            Iterator<Map.Entry<K,V>> i = this.entrySet().iterator();
            while(i.hasNext())
            {
                Map.Entry<K, V> e = i.next();
                K key = e.getKey();
                V value = e.getValue();
                if(t.get(key) == null) { return false; }
                else
                {
                    if(!value.equals(t.get(key))) { return false; }
                }
            }
        }

        catch (ClassCastException | NullPointerException e)
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        if(this.isEmpty()) {return 0;}

        int hc = 0;
        for(Map.Entry<K, V> entry : this.entrySet())
        {
            hc += entry.hashCode();
        }

        return hc;
    }


    @Override
    public String toString()
    {
        StringBuilder cad = new StringBuilder("");
        for(int i = 0; i < this.table.length; i++)
        {
            cad.append("\nMap ").append(i).append(":\n\t").append(this.table[i].toString());
        }
        return cad.toString();
    }


    public boolean contains(Object value)
    {
        if(value == null) return false;

        for(ArrayList<Map.Entry<K, V>> index : this.table)
        {

            Map.Entry<K, V> entry = (Map.Entry<K, V>) index;
            if(value.equals(entry.getValue())) return true;

        }
        return false;
    }


    protected void rehash()
    {
        int old_length = this.table.length;

        // nuevo tamaño: se dobla el tamaño anterior y se encuentra el siguiente numero primo
        int new_length = old_length * 2 + 1;
        if (!esPrimo(new_length))
        {
            new_length = siguientePrimo(new_length);
        }

        // no permitir que la tabla tenga un tamaño mayor al límite máximo...
        // ... para evitar overflow y/o desborde de índices...
        if(new_length > TSB_OAHashtable.MAX_SIZE)
        {
            new_length = TSB_OAHashtable.MAX_SIZE;
        }

        // crear el nuevo arreglo con new_length listas vacías...
        ArrayList<Map.Entry<K,V>> temp[] = new ArrayList[new_length];

        // notificación fail-fast iterator... la tabla cambió su estructura...
        this.modCount++;

        // recorrer el viejo arreglo y redistribuir los objetos que tenia...
        for(int i = 0; i < this.table.length; i++)
        {
                // obtener un objeto de la vieja lista...
                Map.Entry<K, V> x = (Map.Entry<K, V>) this.table[i];

                // obtener su nuevo valor de dispersión para el nuevo arreglo...
                K key = x.getKey();
                int y = this.h(key, temp.length);

                // insertarlo en el nuevo arreglo, en la lista numero "y"...
                temp[y]=(ArrayList<Map.Entry<K, V>>) x;
        }


        // cambiar la referencia table para que apunte a temp...
        this.table = temp;
    }


    //Metodos privados

    private int h(int k)
    {
        return h(k, this.table.length);
    }

    private int h(K key)
    {
        return h(key.hashCode(), this.table.length);
    }

    private int h(K key, int t)
    {
        return h(key.hashCode(), t);
    }

    private int h(int k, int t)
    {
        if(k < 0) k *= -1;
        return k % t;
    }

    private int averageLength()
    {
        return this.count / this.table.length;
    }

    /*private Map.Entry<K, V> search_for_entry(K key)
    {
        Iterator<Map.Entry<K, V>> it = this.entrySet.iterator();
        while(it.hasNext())
        {
            Map.Entry<K, V> entry = it.next();
            if(key.equals(entry.getKey())) return entry;
        }
        return null;
    }*/

    /*private int search_for_index(K key)
    {
        Iterator<Map.Entry<K, V>> it = this.entrySet.iterator();
        for(int i=0; it.hasNext(); i++)
        {
            Map.Entry<K, V> entry = it.next();
            if(key.equals(entry.getKey())) return i;
        }
        return -1;
    }*/


    public static boolean esPrimo(int num) {
        if (num <= 1) {
            return false;
        }
        for (int i = 2; i <= Math.sqrt(num); i++) {
            if (num % i == 0) {
                return false;
            }
        }
        return true;
    }


    private static final int siguientePrimo ( int n )
    {
        if ( n % 2 == 0) n++;
        for ( ; !esPrimo(n); n+=2 ) ;
        return n;
    }


    private static final int siguienteIndex(int i, int j)
    {
        return i + j*j;
    }

    //Clases internas

    private class Entry<K, V> implements Map.Entry<K, V>
    {
        private K key;
        private V value;

        public Entry(K key, V value)
        {
            if(key == null || value == null)
            {
                throw new IllegalArgumentException("Entry(): parámetro null...");
            }
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey()
        {
            return key;
        }

        @Override
        public V getValue()
        {
            return value;
        }

        @Override
        public V setValue(V value)
        {
            if(value == null)
            {
                throw new IllegalArgumentException("setValue(): parámetro null...");
            }

            V old = this.value;
            this.value = value;
            return old;
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 61 * hash + Objects.hashCode(this.key);
            hash = 61 * hash + Objects.hashCode(this.value);
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (this.getClass() != obj.getClass()) { return false; }

            final Entry other = (Entry) obj;
            if (!Objects.equals(this.key, other.key)) { return false; }
            if (!Objects.equals(this.value, other.value)) { return false; }
            return true;
        }

        @Override
        public String toString()
        {
            return "(" + key.toString() + ", " + value.toString() + ")";
        }
    }


    private class KeySet extends AbstractSet<K>
    {
        @Override
        public Iterator<K> iterator()
        {
            return new KeySetIterator();
        }

        @Override
        public int size()
        {
            return TSB_OAHashtable.this.count;
        }

        @Override
        public boolean contains(Object o)
        {
            return TSB_OAHashtable.this.containsKey(o);
        }

        @Override
        public boolean remove(Object o)
        {
            return (TSB_OAHashtable.this.remove(o) != null);
        }

        @Override
        public void clear()
        {
            TSB_OAHashtable.this.clear();
        }

        private class KeySetIterator implements Iterator<K>
        {
            // índice de la lista actualmente recorrida...
            private int current_index;

            // índice de la lista anterior (si se requiere en remove())...
            private int last_index;

            // índice del elemento actual en el iterador (el que fue retornado
            // la última vez por next() y será eliminado por remove())...
            //private int current_entry;

            // flag para controlar si remove() está bien invocado...
            private boolean next_ok;

            // el valor que debería tener el modCount de la tabla completa...
            private int expected_modCount;

            /*
             * Crea un iterador comenzando en la primera lista. Activa el
             * mecanismo fail-fast.
             */
            public KeySetIterator()
            {
                current_index = 0;
                last_index = 0;
                //current_entry = -1;
                next_ok = false;
                expected_modCount = TSB_OAHashtable.this.modCount;
            }

            /*
             * Determina si hay al menos un elemento en la tabla que no haya
             * sido retornado por next().
             */
            @Override
            public boolean hasNext()
            {
                // variable auxiliar t para simplificar accesos...
                ArrayList<Map.Entry<K, V>> t[] = TSB_OAHashtable.this.table;

                if(TSB_OAHashtable.this.isEmpty()) { return false; }
                if(current_index >= t.length) { return false; }

                // bucket actual vacío o listo?...
                if(t[current_index].isEmpty()) //current_entry >= t[current_bucket].size() - 1)
                {
                    // ... -> ver el siguiente bucket no vacío...
                    int next_index = current_index + 1;
                    while(next_index < t.length && t[next_index].isEmpty())
                    {
                        next_index++;
                    }
                    if(next_index >= t.length) { return false; }
                }

                // en principio alcanza con esto... revisar...
                return true;
            }

            /*
             * Retorna el siguiente elemento disponible en la tabla.
             */
            @Override
            public K next()
            {
                // control: fail-fast iterator...
                if(TSB_OAHashtable.this.modCount != expected_modCount)
                {
                    throw new ConcurrentModificationException("next(): modificación inesperada de tabla...");
                }

                if(!hasNext())
                {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                // variable auxiliar t para simplificar accesos...
                ArrayList<Map.Entry<K, V>> t[] = TSB_OAHashtable.this.table;

                // se puede seguir en el mismo bucket?...

                //if(t[current_index].isEmpty()) //&& current_entry < bucket.size() - 1) { current_entry++; }
                //{
                    // si no se puede...
                    // ...recordar el índice del bucket que se va a abandonar..
                    last_index = current_index;

                    // buscar el siguiente bucket no vacío, que DEBE existir, ya
                    // que se hasNext() retornó true...
                    //current_index++;
                    while(t[current_index].isEmpty())
                    {
                        current_index++;
                    }

                    // actualizar la referencia bucket con el núevo índice...
                    //bucket = t[current_bucket];

                    // y posicionarse en el primer elemento de ese bucket...
                    //current_entry = 0;
                //}

                // avisar que next() fue invocado con éxito...
                next_ok = true;

                // y retornar la clave del elemento alcanzado...
                Map.Entry<K, V> objeto = (Map.Entry<K, V>) t[current_index];
                K key = objeto.getKey();
                return key;
            }

            @Override
            public void remove()
            {
                if(!next_ok)
                {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                // eliminar el objeto que retornó next() la última vez...
                Map.Entry<K, V> garbage = (Map.Entry<K, V>) TSB_OAHashtable.this.table[current_index];
                TSB_OAHashtable.this.remove(garbage.getKey());

                // quedar apuntando al anterior al que se retornó...
                //if(last_bucket != current_bucket)
                //{
                //    current_bucket = last_bucket;
                //    current_entry = TSB_OAHashtable.this.table[current_bucket].size() - 1;
                //}
                current_index = last_index;

                // avisar que el remove() válido para next() ya se activó...
                next_ok = false;

                // la tabla tiene un elemento menos...
                TSB_OAHashtable.this.count--;

                // fail_fast iterator: todo en orden...
                TSB_OAHashtable.this.modCount++;
                expected_modCount++;
            }
        }
    }


    private class EntrySet extends AbstractSet<Map.Entry<K, V>>
    {

        @Override
        public Iterator<Map.Entry<K, V>> iterator()
        {
            return new EntrySetIterator();
        }

        /*
         * Verifica si esta vista (y por lo tanto la tabla) contiene al par
         * que entra como parámetro (que debe ser de la clase Entry).
         */
        @Override
        public boolean contains(Object o)
        {
            if(o == null) { return false; }
            if(!(o instanceof Entry)) { return false; }

            Map.Entry<K, V> entry = (Map.Entry<K,V>)o;
            K key = entry.getKey();
            int index = TSB_OAHashtable.this.h(key);

            Map.Entry<K, V> map = (Map.Entry<K, V>) TSB_OAHashtable.this.table[index];
            int j = 1;
            while (index <= TSB_OAHashtable.this.table.length)
            {
                if(map.equals(entry)){ return true; }
                else
                {
                    map = (Map.Entry<K, V>) TSB_OAHashtable.this.table[siguienteIndex(index,j)];
                    j++;
                }
            }
            return false;
        }

        /*
         * Elimina de esta vista (y por lo tanto de la tabla) al par que entra
         * como parámetro (y que debe ser de tipo Entry).
         */
        @Override
        public boolean remove(Object o)
        {
            if(o == null) { throw new NullPointerException("remove(): parámetro null");}
            if(!(o instanceof Entry)) { return false; }

            Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
            K key = entry.getKey();
            int index = TSB_OAHashtable.this.h(key);
            Map.Entry<K, V> map = (Map.Entry<K, V>) TSB_OAHashtable.this.table[index];
            int j = 1;
            while (index <= TSB_OAHashtable.this.table.length)
            {
                if(map.equals(entry))
                {
                    TSB_OAHashtable.this.remove(key);
                    TSB_OAHashtable.this.count--;
                    TSB_OAHashtable.this.modCount++;
                    return true;
                }
                else
                {
                    map = (Map.Entry<K, V>) TSB_OAHashtable.this.table[siguienteIndex(index,j)];
                    j++;
                }
            }
            return false;
        }

        @Override
        public int size()
        {
            return TSB_OAHashtable.this.count;
        }

        @Override
        public void clear()
        {
            TSB_OAHashtable.this.clear();
        }

        private class EntrySetIterator implements Iterator<Map.Entry<K, V>>
        {
            // índice de la lista actualmente recorrida...
            private int current_index;

            // índice de la lista anterior (si se requiere en remove())...
            private int last_index;

            // índice del elemento actual en el iterador (el que fue retornado
            // la última vez por next() y será eliminado por remove())...
            //private int current_entry;

            // flag para controlar si remove() está bien invocado...
            private boolean next_ok;

            // el valor que debería tener el modCount de la tabla completa...
            private int expected_modCount;

            /*
             * Crea un iterador comenzando en la primera lista. Activa el
             * mecanismo fail-fast.
             */
            public EntrySetIterator()
            {
                current_index = 0;
                last_index = 0;
                //current_entry = -1;
                next_ok = false;
                expected_modCount = TSB_OAHashtable.this.modCount;
            }

            /*
             * Determina si hay al menos un elemento en la tabla que no haya
             * sido retornado por next().
             */
            @Override
            public boolean hasNext()
            {
                // variable auxiliar t para simplificar accesos...
                ArrayList<Map.Entry<K, V>> t[] = TSB_OAHashtable.this.table;

                if(TSB_OAHashtable.this.isEmpty()) { return false; }
                if(current_index >= t.length) { return false; }

                // bucket actual vacío o listo?...
                while(t[current_index].isEmpty())
                {
                    current_index++;
                    if (current_index >= t.length){return false;}
                }

                // en principio alcanza con esto... revisar...
                return true;
            }

            /*
             * Retorna el siguiente elemento disponible en la tabla.
             */
            @Override
            public Map.Entry<K, V> next()
            {
                // control: fail-fast iterator...
                if(TSB_OAHashtable.this.modCount != expected_modCount)
                {
                    throw new ConcurrentModificationException("next(): modificación inesperada de tabla...");
                }

                if(!hasNext())
                {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                // variable auxiliar t para simplificar accesos...
                ArrayList<Map.Entry<K, V>> t[] = TSB_OAHashtable.this.table;

                // se puede seguir en el mismo bucket?...
                //ArrayList<Map.Entry<K, V>> bucket = t[current_bucket];
                //if(!t[current_bucket].isEmpty() && current_entry < bucket.size() - 1) { current_entry++; }
                //else
                //{
                    // si no se puede...
                    // ...recordar el índice del bucket que se va a abandonar..
                last_index = current_index;

                    // buscar el siguiente bucket no vacío, que DEBE existir, ya
                    // que se hasNext() retornó true...
                    //current_bucket++;
                while(t[current_index].isEmpty())
                {
                    current_index++;
                }

                    // actualizar la referencia bucket con el núevo índice...
                    //bucket = t[current_bucket];

                    // y posicionarse en el primer elemento de ese bucket...
                    //current_entry = 0;
                //}

                // avisar que next() fue invocado con éxito...
                next_ok = true;

                // y retornar el elemento alcanzado...
                return (Map.Entry<K, V>) t[current_index];
            }

            /*
             * Remueve el elemento actual de la tabla, dejando el iterador en la
             * posición anterior al que fue removido. El elemento removido es el
             * que fue retornado la última vez que se invocó a next(). El método
             * sólo puede ser invocado una vez por cada invocación a next().
             */
            @Override
            public void remove()
            {
                if(!next_ok)
                {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                // eliminar el objeto que retornó next() la última vez...
                Map.Entry<K, V> garbage = (Map.Entry<K, V>) TSB_OAHashtable.this.table[current_index];
                TSB_OAHashtable.this.remove(garbage.getKey());

                // quedar apuntando al anterior al que se retornó...
                current_index = last_index;

                // avisar que el remove() válido para next() ya se activó...
                next_ok = false;

                // la tabla tiene un elementon menos...
                TSB_OAHashtable.this.count--;

                // fail_fast iterator: todo en orden...
                TSB_OAHashtable.this.modCount++;
                expected_modCount++;
            }
        }
    }


    private class ValueCollection extends AbstractCollection<V>
    {
        @Override
        public Iterator<V> iterator()
        {
            return new ValueCollectionIterator();
        }

        @Override
        public int size()
        {
            return TSB_OAHashtable.this.count;
        }

        @Override
        public boolean contains(Object o)
        {
            return TSB_OAHashtable.this.containsValue(o);
        }

        @Override
        public void clear()
        {
            TSB_OAHashtable.this.clear();
        }

        private class ValueCollectionIterator implements Iterator<V>
        {
            // índice de la lista actualmente recorrida...
            private int current_index;

            // índice de la lista anterior (si se requiere en remove())...
            private int last_index;

            // índice del elemento actual en el iterador (el que fue retornado
            // la última vez por next() y será eliminado por remove())...
            //private int current_entry;

            // flag para controlar si remove() está bien invocado...
            private boolean next_ok;

            // el valor que debería tener el modCount de la tabla completa...
            private int expected_modCount;

            /*
             * Crea un iterador comenzando en la primera lista. Activa el
             * mecanismo fail-fast.
             */
            public ValueCollectionIterator()
            {
                current_index = 0;
                last_index = 0;
//                current_entry = -1;
                next_ok = false;
                expected_modCount = TSB_OAHashtable.this.modCount;
            }

            /*
             * Determina si hay al menos un elemento en la tabla que no haya
             * sido retornado por next().
             */
            @Override
            public boolean hasNext()
            {
                // variable auxiliar t para simplificar accesos...
                ArrayList<Map.Entry<K, V>> t[] = TSB_OAHashtable.this.table;

                if(TSB_OAHashtable.this.isEmpty()) { return false; }
                if(current_index >= t.length) { return false; }

                // bucket actual vacío o listo?...
                while(t[current_index].isEmpty())
                {
                    current_index++;
                    if (current_index >= t.length){return false;}
                }

                // en principio alcanza con esto... revisar...
                return true;
            }

            /*
             * Retorna el siguiente elemento disponible en la tabla.
             */
            @Override
            public V next()
            {
                // control: fail-fast iterator...
                if(TSB_OAHashtable.this.modCount != expected_modCount)
                {
                    throw new ConcurrentModificationException("next(): modificación inesperada de tabla...");
                }

                if(!hasNext())
                {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                // variable auxiliar t para simplificar accesos...
                ArrayList<Map.Entry<K, V>> t[] = TSB_OAHashtable.this.table;

                // se puede seguir en el mismo bucket?...
                Map.Entry<K, V> map = (Map.Entry<K, V>) t[current_index];
                //if(!t[current_bucket].isEmpty() && current_entry < bucket.size() - 1) { current_entry++; }
                //else
                //{
                    // si no se puede...
                    // ...recordar el índice del bucket que se va a abandonar..
                last_index = current_index;

                    // buscar el siguiente bucket no vacío, que DEBE existir, ya
                    // que se hasNext() retornó true...
                while(t[current_index].isEmpty())
                {
                    current_index++;
                }

                // avisar que next() fue invocado con éxito...
                next_ok = true;

                // y retornar la clave del elemento alcanzado...
                Map.Entry<K, V> objeto = (Map.Entry<K, V>) t[current_index];
                V value = objeto.getValue();
                return value;
            }

            /*
             * Remueve el elemento actual de la tabla, dejando el iterador en la
             * posición anterior al que fue removido. El elemento removido es el
             * que fue retornado la última vez que se invocó a next(). El método
             * sólo puede ser invocado una vez por cada invocación a next().
             */
            @Override
            public void remove()
            {
                if(!next_ok)
                {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                // eliminar el objeto que retornó next() la última vez...
                Map.Entry<K, V> garbage = (Map.Entry<K, V>) TSB_OAHashtable.this.table[current_index];
                TSB_OAHashtable.this.remove(garbage.getKey());

                // avisar que el remove() válido para next() ya se activó...
                next_ok = false;

                // la tabla tiene un elementon menos...
                TSB_OAHashtable.this.count--;

                // fail_fast iterator: todo en orden...
                TSB_OAHashtable.this.modCount++;
                expected_modCount++;
            }
        }
    }
}