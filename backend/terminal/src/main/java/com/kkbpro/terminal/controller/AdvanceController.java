package com.kkbpro.terminal.controller;

import com.kkbpro.terminal.constants.enums.FileBlockStateEnum;
import com.kkbpro.terminal.consumer.WebSocketServer;
import com.kkbpro.terminal.pojo.dto.CooperateInfo;
import com.kkbpro.terminal.result.Result;
import com.kkbpro.terminal.utils.AesUtil;
import com.kkbpro.terminal.utils.StringUtil;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 高级功能接口类
 */
@RestController
@RequestMapping("/api")
public class AdvanceController {

    public static final String COOPERATE_SECRET_KEY = "o4D1fYuVp2js9xKX";

    public static final String[] DOCKER_INFO_CMD = new String[]{
            "echo -n \"$(docker ps --format \"{{.ID}}@{{.Names}}@{{.Status}}@{{.Image}}@{{.Ports}}\" | paste -sd '$' -)\"",
            "echo -n \"$(docker images --format \"{{.ID}}@{{.Repository}}@{{.Size}}@{{.CreatedAt}}\" | paste -sd '$' -)\"",
            "echo -n \"$(docker network ls --format \"{{.Name}}\" | xargs -I {} docker network inspect {} --format \"{{.Name}}@{{range .IPAM.Config}}{{.Subnet}}{{end}}@{{range .IPAM.Config}}{{.Gateway}}{{end}}@{{.Created}}\" | paste -sd'$' -)\"",
            "echo -n \"$(docker volume ls --format \"{{.Name}}\" | xargs -I {} docker volume inspect {} --format \"{{.Name}}@{{.Mountpoint}}@{{.CreatedAt}}\" | paste -sd'$' -)\"\n"
    };

    /**
     * 获取协作Key
     */
    @GetMapping("/cooperate")
    public Result cooperate(String sshKey, Boolean readOnly, Integer maxHeadCount) throws Exception {
        String errorMsg = "协作Key生成失败";
        String successMsg = "协作Key生成成功";

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        WebSocketServer webSocketServer = WebSocketServer.webSocketServerMap.get(sshKey);
        if(ssh == null || webSocketServer == null) {
            return Result.error(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，" + errorMsg);
        }
        if(webSocketServer.getCooperateInfo() != null)
            return Result.error(errorMsg);

        CooperateInfo cooperateInfo = new CooperateInfo();
        cooperateInfo.setReadOnly(readOnly);
        cooperateInfo.setMaxHeadCount(maxHeadCount);
        webSocketServer.setCooperateInfo(cooperateInfo);

        String key = StringUtil.changeBase64Str(AesUtil.aesEncrypt(sshKey, COOPERATE_SECRET_KEY));

        return Result.success(successMsg, key);
    }

    //    完整命令
    //    echo -n "$(uptime | awk -F'load average: ' '{print $2}' | awk '{print $1}' | sed 's/,//')@" && \
    //    echo -n "$(top -bn1 | grep "Cpu(s)" | sed "s/.*, *\([0-9.]*\)%* id.*/\1/" | awk '{print 100 - $1}')@" && \
    //    echo -n "$(nproc)@" && \
    //    echo -n "$(free -m | grep Mem | awk '{print $3"@"$2}')@" && \
    //    echo -n "$(df -BMB / | grep / | awk '{print $3"@"$2}' | sed 's/[A-Za-z]//g')" && \
    //    echo -n "^" && \
    //    echo -n "$(ps -eo pid,user,%cpu,%mem,comm --sort=-%cpu | head -n 5 | awk 'NR>1 {print $1"@"$2"@"$3"@"$4"@"$5}' | tr '\n' '$' | sed 's/\(.*\)\$$/\1/')" && \
    //    echo -n "^" && \
    //    echo -n "$(cat /proc/net/dev | awk 'NR>2 {up+=$10; down+=$2;} END {print "all:@"up"@"down}')" && \
    //    echo -n "$" && \
    //    echo -n "$(cat /proc/net/dev | awk 'NR>2 {print $1"@"$10"@"$2}' | tr -s ' ' '@' | tr '\n' '$' | sed 's/\(.*\)\$/\1/')" && \
    //    echo -n "^" && \
    //    echo -n "$(cat /proc/diskstats | awk '{if($3 ~ /[^0-9]$/) {read+=$6; write+=$10}} END {print "all@" read"@"write}')" && \
    //    echo -n "$" && \
    //    echo -n "$(cat /proc/diskstats | awk '{print $3"@"$6"@"$10}' | tr -s ' ' '@' | tr '\n' '$' | sed 's/\(.*\)\$/\1/')" && \
    //    echo -n "^" && \
    //    echo -n $(date +%s%3N)
    /**
     * 获取监控状态
     */
    @PostMapping("/monitor")
    public Result monitor(String sshKey) {
        String errorMsg = "获取监控状态失败";
        String successMsg = "获取监控状态成功";

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.error(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，" + errorMsg);
        }
        String status = "";
        String command = "echo -n \"$(uptime | awk -F'load average: ' '{print $2}' | awk '{print $1}' | sed 's/,//')@\" && \\\n" +
                "echo -n \"$(top -bn1 | grep \"Cpu(s)\" | sed \"s/.*, *\\([0-9.]*\\)%* id.*/\\1/\" | awk '{print 100 - $1}')@\" && \\\n" +
                "echo -n \"$(nproc)@\" && \\\n" +
                "echo -n \"$(free -m | grep Mem | awk '{print $3\"@\"$2}')@\" && \\\n" +
                "echo -n \"$(df -BMB / | grep / | awk '{print $3\"@\"$2}' | sed 's/[A-Za-z]//g')\" && \\\n" +
                "echo -n \"^\" && \\\n" +
                "echo -n \"$(ps -eo pid,user,%cpu,%mem,comm --sort=-%cpu | head -n 5 | awk 'NR>1 {print $1\"@\"$2\"@\"$3\"@\"$4\"@\"$5}' | tr '\\n' '$' | sed 's/\\(.*\\)\\$$/\\1/')\" && \\\n" +
                "echo -n \"^\" && \\\n" +
                "echo -n \"$(cat /proc/net/dev | awk 'NR>2 {up+=$10; down+=$2;} END {print \"all:@\"up\"@\"down}')\" && \\\n" +
                "echo -n \"$\" && \\\n" +
                "echo -n \"$(cat /proc/net/dev | awk 'NR>2 {print $1\"@\"$10\"@\"$2}' | tr -s ' ' '@' | tr '\\n' '$' | sed 's/\\(.*\\)\\$/\\1/')\" && \\\n" +
                "echo -n \"^\" && \\\n" +
                "echo -n \"$(cat /proc/diskstats | awk '{if($3 ~ /[^0-9]$/) {read+=$6; write+=$10}} END {print \"all@\" read\"@\"write}')\" && \\\n" +
                "echo -n \"$\" && \\\n" +
                "echo -n \"$(cat /proc/diskstats | awk '{print $3\"@\"$6\"@\"$10}' | tr -s ' ' '@' | tr '\\n' '$' | sed 's/\\(.*\\)\\$/\\1/')\" && \\\n" +
                "echo -n \"^\" && \\\n" +
                "echo -n $(date +%s%3N)";
        try(Session session = ssh.startSession();
            Session.Command cmd = session.exec(command))
        {
            // 读取命令执行结果
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(cmd.getInputStream()))) {
                status += reader.readLine();
            }
            // 等待命令执行完毕
            cmd.join();
            int exitStatus = cmd.getExitStatus();
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(errorMsg);
        }
        return Result.success(200, successMsg, status);
    }

    // 完整命令
    //    echo -n "$(docker ps --format "{{.ID}}@{{.Names}}@{{.Status}}@{{.Image}}@{{.Ports}}" | paste -sd '$' -)" && \
    //    echo -n "^" && \
    //    echo -n "$(docker images --format "{{.ID}}@{{.Repository}}@{{.Size}}@{{.CreatedAt}}" | paste -sd '$' -)" && \
    //    echo -n "^" && \
    //    echo -n "$(docker network ls --format "{{.Name}}" | xargs -I {} docker network inspect {} --format "{{.Name}}@{{range .IPAM.Config}}{{.Subnet}}{{end}}@{{range .IPAM.Config}}{{.Gateway}}{{end}}@{{.Created}}" | paste -sd'$' -)" && \
    //    echo -n "^" && \
    //    echo -n "$(docker volume ls --format "{{.Name}}" | xargs -I {} docker volume inspect {} --format "{{.Name}}@{{.Mountpoint}}@{{.CreatedAt}}" | paste -sd'$' -)"
    /**
     * 获取Docker信息
     */
    @PostMapping("/docker/info")
    public Result dockerContainerInfo(String sshKey, Integer type) {
        String errorMsg = "获取Docker信息失败";
        String successMsg = "获取Docker信息成功";

        SSHClient ssh = WebSocketServer.sshClientMap.get(sshKey);
        if(ssh == null) {
            return Result.error(FileBlockStateEnum.SSH_NOT_EXIST.getState(),"连接断开，" + errorMsg);
        }
        String docker = "";
        String command = DOCKER_INFO_CMD[type % DOCKER_INFO_CMD.length];
        try(Session session = ssh.startSession();
            Session.Command cmd = session.exec(command))
        {
            // 读取命令执行结果
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(cmd.getInputStream()))) {
                docker += reader.readLine();
            }
            // 等待命令执行完毕
            cmd.join();
            int exitStatus = cmd.getExitStatus();
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(errorMsg);
        }
        return Result.success(200, successMsg, docker);
    }

}
