/**
 * Copyright 2015-2020 jiuxian.com.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jiuxian.theone;

public interface Competitive extends AutoCloseable {

	/**
	 * Try to fetch the lock
	 */
	void fetchLock();

	/**
	 * The one who has got the lock currently
	 * 
	 * @return the locker
	 */
	String currentLocker();

	/**
	 * Register correlative resource that must be closed before the competitive
	 * implementation shutdown
	 * 
	 * @param resource
	 */
	void registerCorrelativeResource(AutoCloseable resource);

}