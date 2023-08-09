package ru.nightmare;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import static ru.nightmare.Algorithms.*;

/**
 * Boot класс, отвечает за загрузку программы, хранит в себе глобальные переменные
 */
public class Boot {

    /**
     * Порядок сортировки, по умолчанию прямой
     */
    public static Boolean forwardSort = true;
    /**
     * Режим сортировки, строки(true) или числа(false)
     * Если не задано, приводит к сообщению об ошибке и завершению программы
     */
    public static Boolean stroke;
    /**
     * имена исходных файлов
     */
    public static ArrayList<String> input = new ArrayList<>();
    /**
     * имена файлов для записи результата
     */
    public static ArrayList<String> output = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(Boot.class);
    /**
     * main метод программы, принимает и обрабатывает аргументы в самом начале.
     * Проверяет валидность аргументов и возможность перехода к основной части,
     * если это невозможно, завершает программу с соответствующей ошибкой.
     * Чистит от несуществующих файлов перед запуском и тд...
     * Вызывает основную часть функцией process()
     */
    public static void main(String args[]) throws Exception {
        //System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        //System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
        for (String arg: args) {
            logger.debug("Обрабатываю аргумент: "+arg);
            switch (arg) {
                case "-i":
                    stroke = false;
                    break;
                case "-s":
                    stroke = true;
                    break;
                case "-a":
                    forwardSort = true;
                    break;
                case "-d":
                    forwardSort = false;
                    break;
                case "":
                    break;
                default:
                    if (arg.contains("out"))
                        output.add(arg);
                    else
                        input.add(arg);
            }
        }

        logger.info("Проверяю выбор режима строки/числа");
        if (stroke==null) {
            logger.error("Ошибка, не выбран режим строк или чисел");
            System.exit(-3);
        }
        logger.info("Проверяю заданность файлов с исходными данными");
        if (input.size()==0) {
            logger.error("Ошибка, ни единый файл с исходными данными не был задан");
            System.exit(-4);
        }
        logger.info("Проверяю заданность файлов выхода");
        if (output.size()==0) {
            logger.error("Ошибка, ни единый выходной файл не был задан");
            System.exit(-5);
        }

        logger.info("Проверяю наличие файлов с исходными данными");
        ArrayList<Reader> readers = new ArrayList<>();
        ArrayList<Writer> writers = new ArrayList<>();


        for (String str: input) {
            try {
                Reader r = new Reader(str);
                readers.add(r);
            } catch (Exception e) {
                 logger.warn("Файл "+str+" недоступен для работы и не будет задействован");
            }
        }
        for (String str: output) {
            writers.add(new Writer(str));
        }

        logger.info("Начинаю выполнение операций");
        process(readers, writers);

    }

        /**
         * process - обрабатывает и записывает данные из исходных файлов в выходные.
         * Использует для этого алгоритмы из класса Algorithms
         * Состоит из фазы подготовки и бесконечного цикла идущего до окончания каждого файла и данных в буферах.
         * Программа нормально завершается в этой функции.
         * Иные способы нормального завершения и продолжения не предусмотрены
         */
        public static void process(ArrayList<Reader> readers, ArrayList<Writer> writers) throws IOException {
            logger.info("Инициирую начало работы");
            removeFailedIOs(readers, writers);
            update(readers);
            exitWhenDone(readers, writers);
            String current;
            ArrayList<String> buffers = getBuffers(readers);

            logger.info("Приступаю");
            while (true) {
                removeNulls(buffers);
                removeInvalid(buffers);
                removeEmptyLines(buffers);
                removeFailedIOs(readers, writers);
                if (buffers.isEmpty())
                    exitWhenDone(readers, writers);
                if (forwardSort) {
                    current = sort(buffers).get(0);
                } else {

                    current = sort(buffers).get(buffers.size()-1>0?buffers.size()-1:0);
                }
                while (!buffers.isEmpty()) {
                    boolean containEquals = false;
                    for (String b : buffers) {
                        if (b.equals(current)) {
                            containEquals = true;
                            for (Writer w : writers) {
                                w.append(current + "\n");
                            }
                        }
                    }
                    String finalCurrent = current;
                    buffers.removeIf(s -> s.equals(finalCurrent));
                    if (!containEquals) break;
                }
                /*while (true) {
                    for (Reader r: readers) {
                        if (r.getBuffer().equals(current))
                            while (current.equals(readTillValid(r))) {
                                for (Writer w : writers) {
                                    System.out.println(current);
                                    w.append(current+"\n");
                                }
                            }
                    }*/
                update(readers);
                buffers.addAll(getBuffers(readers));
            }
        }


}
