package com.example.rgzgolos;

//Класс для представления данных о цветке.

public class Flower {
    private String name;
    private String imagePath; // Относительный путь к файлу изображения в папке /images/
    private String description;

    /**
     * Конструктор для создания объекта цветка.
     *  name Название цветка.
     * imagePath Путь к файлу изображения.
     * description Краткое описание цветка.
     */
    public Flower(String name, String imagePath, String description) {
        this.name = name;
        this.imagePath = imagePath;
        this.description = description;
    }


    public String getName() {
        return name;
    }


    public String getImagePath() {
        return imagePath;
    }


    public String getDescription() {
        return description;
    }
}
