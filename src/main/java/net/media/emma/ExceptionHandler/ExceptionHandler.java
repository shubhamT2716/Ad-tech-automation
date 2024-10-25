package net.media.emma.ExceptionHandler;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serial;


@NoArgsConstructor
@AllArgsConstructor
public class ExceptionHandler extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    String message;


}
