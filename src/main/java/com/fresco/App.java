package com.fresco;

import java.util.List;
import java.util.Properties;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;

public class App {

	public static void main(String[] args) throws InterruptedException {

		var properties = new MyProperties()//
				.setProperty("DOCKER_CERT_PATH", "/home/jairo/.docker/")//
				.setProperty("DOCKER_TLS_VERIFY", "1")//
				.setProperty("DOCKER_HOST", "tcp://10.0.0.121:2375");

		var config = new DefaultDockerClientConfig.Builder().build();
		var dockerClient = DockerClientBuilder.getInstance(config).build();
		var images = dockerClient.listImagesCmd().exec();
		for (var image : images) {
			var tags = List.of(image.getRepoTags());
			System.out.println(image.getId() + " " + tags);
		}
		var containers = dockerClient.listContainersCmd().exec();
		for (var container : containers) {
			var names = List.of(container.getNames());
			System.out.println(container.getId() + " " + names);
		}
		//Thread.sleep(1_000);
		System.out.println("end");
	}

	static class MyProperties  {

		private Properties properties = new Properties();
		
		public MyProperties setProperty(String key, String value) {
			properties.put(key, value);
			return this;
		}
	}
}
