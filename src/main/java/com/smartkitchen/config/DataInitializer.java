package com.smartkitchen.config;

import com.smartkitchen.entity.Chef;
import com.smartkitchen.entity.MenuItem;
import com.smartkitchen.repository.ChefRepository;
import com.smartkitchen.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ChefRepository chefRepository;
    private final MenuItemRepository menuRepository;

    @Override
    public void run(String... args) {

        if (chefRepository.count() == 0) {

            chefRepository.saveAll(List.of(

                    Chef.builder().name("Chef Ram").build(),

                    Chef.builder().name("Chef Maya").build(),

                    Chef.builder().name("Chef Ravi").build(),

                    Chef.builder().name("Chef Arjun").build(),

                    Chef.builder().name("Chef Sara").build()

            ));
        }

        if (menuRepository.count() == 0) {

            menuRepository.saveAll(List.of(

                    MenuItem.builder()
                            .name("Burger")
                            .cookTime(8)
                            .failureRate(20)
                            .build(),

                    MenuItem.builder()
                            .name("Fries")
                            .cookTime(5)
                            .failureRate(10)
                            .build(),

                    MenuItem.builder()
                            .name("Pizza")
                            .cookTime(12)
                            .failureRate(30)
                            .build(),

                    MenuItem.builder()
                            .name("Coke")
                            .cookTime(2)
                            .failureRate(5)
                            .build(),

                    MenuItem.builder()
                            .name("Pasta")
                            .cookTime(10)
                            .failureRate(15)
                            .build()

            ));
        }

        System.out.println("SmartKitchen data initialized");
    }
}