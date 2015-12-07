package ru.taximaxim.pgsqlblocks;

public class Settings {
    
    private int updatePeriod;
    
    private static Settings instance;
    
    private Settings() {
        //Инициализируем период обновления
        this.updatePeriod = 10;
    }
    
    public static Settings getInstance() {
        if(instance == null) {
            instance = new Settings();
        }
        return instance;
    }
    
    /**
     * Устанавливаем период обновления
     * @param updatePeriod
     */
    public void setUpdatePeriod(int updatePeriod) {
        this.updatePeriod = updatePeriod;
    }
    
    /**
     * Получаем период обновления
     * @return
     */
    public int getUpdatePeriod() {
        return updatePeriod;
    }
}
