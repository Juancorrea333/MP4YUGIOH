package patterns;


public interface Comando {

    /**
     * Ejecuta la acción encapsulada.
     * @return mensaje de error si la acción no pudo realizarse, null si fue exitosa.
     */
    String execute();


    void undo();

    
    String getDescripcion();
}
