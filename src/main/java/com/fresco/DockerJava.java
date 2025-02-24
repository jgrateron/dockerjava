package com.fresco;

import java.awt.Desktop;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.Device;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;

public class DockerJava {

	public static void main(String[] args) throws InterruptedException, IOException, URISyntaxException {
		var config = new DefaultDockerClientConfig.Builder().build();
		var dockerClient = DockerClientBuilder.getInstance(config).build();
		var images = dockerClient.listImagesCmd().exec();
		for (var image : images) {
			var tags = List.of(image.getRepoTags());
			System.out.println(image.getId() + " " + tags);
		}
		var image = "dockurr/windows";
		var port = 8006;
		var hostConfig = new HostConfig()
				.withPrivileged(true)
				.withDevices(Device.parse("/dev/kvm"))
				.withDevices(Device.parse("/dev/net/tun"))
				.withCapAdd(Capability.NET_ADMIN)
				.withPortBindings(PortBinding.parse(port + ":" + port));
		var container = dockerClient.createContainerCmd(image)
				.withEnv("VERSION=http://192.168.1.95/windowsxpsp3.iso")
				.withHostConfig(hostConfig)
				.withExposedPorts(ExposedPort.tcp(port)).exec();
		dockerClient.startContainerCmd(container.getId()).exec();
		var containers = dockerClient.listContainersCmd().exec();
		for (var cont : containers) {
			var names = List.of(cont.getNames());
			System.out.println(cont.getId() + " " + names);
		}
		var latch = new CountDownLatch(1);
		var logContainerCmd = dockerClient.logContainerCmd(container.getId())
				.withStdOut(true)
				.withStdErr(true)
				.withFollowStream(true);
		logContainerCmd.exec(new ResultCallback<Frame>() {
			@Override
			public void onStart(Closeable closeable) {
			}

			@Override
			public void onNext(Frame frame) {
				var msg = new String(frame.getPayload());
				if (!msg.isBlank()) {
					System.out.print(msg);
				}
				if (msg.contains("http://localhost:8006")) {
					latch.countDown();
				}
			}

			@Override
			public void onError(Throwable throwable) {
				throwable.printStackTrace();
				latch.countDown();
			}

			@Override
			public void onComplete() {
				latch.countDown();
			}

			@Override
			public void close() throws IOException {
			}
		});
		latch.await();
		Thread.sleep(2000);
		var desktop = Desktop.getDesktop();
		desktop.browse(new URI("http://localhost:8006"));
		System.out.println("end");
	}

}
