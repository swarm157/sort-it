package ru.nightmare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static ru.nightmare.Boot.stroke;


/**
 * Отдельный класс для размещения в нем статических функций и алгоритмов
 */
public class Algorithms {

    private static final Logger logger = LoggerFactory.getLogger(Algorithms.class);

    /**
     * Извлекает из считывателей последние буфферизованные данные
     * @param readers
     * @return
     * @throws IOException
     */
    public static ArrayList<String> getBuffers(ArrayList<Reader> readers) throws IOException {
        ArrayList<String> result = new ArrayList<>();
        for (Reader r: readers) {
            result.add(r.getBuffer());
        }
        return result;
    }

    /**
     * Читает файл и пропускает строки вплоть до конца файла или той на которой найдет валидное значение
     * @param r
     * @return строка или null если ничего не осталось для считывания
     * @throws IOException
     */
    public static String readTillValid(Reader r) throws IOException {
        while (!r.isEnded()) {
            if (isValid(r.read())) {
                return r.getBuffer();
            }
        }
        return null;
    }

    /**
     * Закрывает и удаляет завершенные считыватели
     * @param readers
     * @throws IOException
     */
    public static void closeEnded(ArrayList<Reader> readers) throws IOException {
        ArrayList<Reader> del = new ArrayList<>();
        for (Reader r: readers) {
            if (r.isEnded()) {
                del.add(r);
                r.close();
            }
        }
        readers.removeAll(del);
    }

    /**
     * Простая реализация бабл алгоритма сортировки
     * Используется, только для чисел
     * @param arr
     */
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    // Swap the elements
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }


    /**
     * Функция распределитель
     * Выбирает каким алгоритмом сортировать
     * Бабл для чисел
     * По Значению первого символа для строк
     * @param lines
     * @return
     */

    public static ArrayList<String> sort(ArrayList<String> lines) {
        if (stroke)
            Collections.sort(lines, new FirstCharacterComparator());
        else {
            int[] arr = new int[lines.size()];
            for (int i = 0; i < lines.size(); i++)
                arr[i] = Integer.parseInt(lines.get(i));
            bubbleSort(arr);
            lines.clear();
            for (int i = 0; i < arr.length; i++)
                lines.add(String.valueOf(arr[i]));
        }
        return lines;
    }

    /**
     * Часть алгоритма сортировки строк по первому символу
     */
    static class FirstCharacterComparator implements Comparator<String> {
        @Override
        public int compare(String str1, String str2) {
            char firstChar1 = str1.charAt(0);
            char firstChar2 = str2.charAt(0);

            return Character.compare(firstChar1, firstChar2);
        }
    }

    /**
     * Обновляет значения в буфере посредством считывания до первого валидного значения в каждом считывателе
     * @param readers
     * @throws IOException
     */

    public static void update(ArrayList<Reader> readers) throws IOException {
        for (Reader r: readers) {
            readTillValid(r);
        }
    }

    /**
     * Убирает nulls
     * @param lines
     */
    public static void removeNulls(ArrayList<String> lines) {
        lines.removeAll(Collections.singleton(null));
    }


    /**
     * Когда не осталось открытых потоков в считывателях, завершает программу
     * @param readers
     * @param writers
     * @throws IOException
     */
    public static void exitWhenDone(ArrayList<Reader> readers, ArrayList<Writer> writers) throws IOException {
        closeEnded(readers);
        if (readers.isEmpty()) {
            logger.info("Готово!!!");
            for (Writer w: writers) {
                w.close();
            }

            logger.info("Программа была успешно завершена");
            System.exit(0);
        }

    }


    /**
     * Удаляет "неудавшиеся" потоки.
     * @param readers
     * @param writers
     */
    public static void removeFailedIOs(ArrayList<Reader> readers, ArrayList<Writer> writers) {
        readers.removeIf(s -> s.isClosed());
        writers.removeIf(s -> s.isClosed());
    }

    /**
     * Удаляет невалидные значения
     * @param lines
     */
    public static void removeInvalid(ArrayList<String> lines) {
        lines.removeIf(s -> !isValid(s));
    }

    /**
     * Удаляет пустые строки
     * @param lines
     */
    public static void removeEmptyLines(ArrayList<String> lines) {
        lines.removeIf(s -> s.equals(""));
    }



    /**
     * Проверяет на валидность
     * Строки с пробелами невалидные
     * Пытается привести к Integer
     * В зависимости от выбранного режима и результата бракует или пропускает строку
     * @param line
     * @return
     */
    public static boolean isValid(String line) {
        if (stroke) {
            try {
                Integer.parseInt(line);
                logger.debug("Строка: "+line+" забракована");
                return false;
            } catch (NumberFormatException e) {
                if (line.contains(" ")) {
                    logger.debug("Строка: "+line+" забракована");
                    return false;
                }
            }
            logger.debug("Строка: "+line+" допущена");
            return true;
        } else {
            try {
                Integer.parseInt(line);
                logger.debug("Строка: "+line+" допущена");

                return true;
            } catch (NumberFormatException e) {
                logger.debug("Строка: "+line+" забракована");
                return false;
            }
        }
    }
}
