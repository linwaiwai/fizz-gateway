/*
 *  Copyright (C) 2020 the original author or authors.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package we.fizz.input;

import we.fizz.component.ComponentHelper;
import org.reflections.Reflections;
import we.fizz.exception.FizzRuntimeException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author linwaiwai
 *
 */
public class InputFactory {
	public static Map<InputType, Class> inputClasses = new HashMap<InputType, Class>();
	public static void registerInput(InputType type, Class inputClass){
		inputClasses.put(type, inputClass);
	}
	public static void unregisterInput(InputType type){
		inputClasses.remove(type);
	}
	public static InputConfig createInputConfig(Map config)  {
		String type = (String) config.get("type");
		InputType typeEnum = InputType.valueOf(type.toUpperCase());
		InputConfig inputConfig = null;
		if (inputClasses.containsKey(typeEnum)){
			Class<?> InputClass = inputClasses.get(typeEnum);

			try {
				Method inputConfigClassMethod = InputClass.getMethod("inputConfigClass");
				Class<?> InputConfigClass = (Class<?>) inputConfigClassMethod.invoke(null);
				Constructor constructor = null;
				constructor = InputConfigClass.getDeclaredConstructor(Map.class);
				constructor.setAccessible(true);
				inputConfig =  (InputConfig) constructor.newInstance(config);
			} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
				throw new FizzRuntimeException(e.getMessage());
			}
			inputConfig.setType(typeEnum);
			inputConfig.setDataMapping((Map<String, Object>) config.get("dataMapping"));
			inputConfig.setComponents(ComponentHelper.buildComponents((List<Map<String, Object>>) config.get("components")));
			inputConfig.parse();
			return inputConfig;
		} else {
			throw new FizzRuntimeException("can't find input config type:" + type);
		}
	}
	
	public static Input createInput(String type) {
		InputType typeEnum = InputType.valueOf(type.toUpperCase());
		Input input = null;
		if (inputClasses.containsKey(typeEnum)) {
			Class<?> InputClass = inputClasses.get(typeEnum);
			Constructor constructor = null;
			try {
				constructor = InputClass.getDeclaredConstructor();
				constructor.setAccessible(true);
				input =  (Input) constructor.newInstance();
				return input;
			} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
				throw new FizzRuntimeException(e.getMessage());
			}
		} else {
			throw new FizzRuntimeException("can't find input type:" + type);
		}
	}

	 public static void loadInputClasses() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Reflections reflections = new Reflections("we.fizz.input");
		Set<Class<? extends Input>> subTypes = reflections.getSubTypesOf(Input.class);
		for (Class<?>inputType : subTypes){
			Method initializeMethod = inputType.getMethod("initialize", Class.class);
			initializeMethod.invoke(null, inputType);
		}
	}
}
