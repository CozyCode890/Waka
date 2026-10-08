# Waka

Một vỏ desktop hình dáng VS Code đặt lên chính các thuật toán của Weka.

> **Trạng thái: chưa có ứng dụng.** Repo này hiện chỉ chứa kế hoạch, bản vẽ UI, và `spike/` —
> mã dùng một lần để chứng minh nền tảng kỹ thuật chạy được trên máy build. Giai đoạn **S01
> (dựng vỏ ứng dụng)** đang làm, chưa có `core/`, `app/` hay `dist/`. Xem [Lộ trình](#lộ-trình).

## Weka là gì, Waka là gì

[Weka](https://ml.cms.waikato.ac.nz/weka/) là bộ thư viện machine learning của Machine Learning
Group, University of Waikato, New Zealand — phát hành dưới GPL-3.0-or-later. Nó mang theo
Explorer, bộ GUI Swing mà hầu hết sinh viên gặp đầu tiên.

Waka là một GUI khác cho cùng bộ thư viện đó. Nó **không** cài đặt lại một thuật toán, filter,
phép đánh giá hay metric nào của Weka — phần phân tích vẫn là của Weka, link thẳng trong cùng
process qua `weka-stable` 3.8.7. Cái tên nói rõ nó là front end cho cái gì, mà không tự nhận
mình là cái đó.

## Vì sao có project này

Weka 3.8.7 đã ship sẵn skin FlatLaf, nên *"cũng thế nhưng đẹp hơn"* không phải là lý do, và
không bao giờ được trở thành lý do. Bốn việc Explorer làm tệ mới là toàn bộ lý do:

| Explorer | Waka |
|---|---|
| `GenericObjectEditor` là hộp thoại modal, chặn đúng cái dataset bạn đang chỉnh tham số cho nó | Không có modal nào. Form tham số là panel ở trong cửa sổ, đóng / tách ra / mở thành tab được |
| Một lần chạy không huỷ được | Việc dài chạy ở subprocess, vì đó là cách duy nhất huỷ thật |
| Result history chết theo process | History sống qua restart, so sánh hai lần chạy cạnh nhau |
| Mỗi lần một dataset | Nhiều dataset mở cùng lúc, mỗi cái một document tab |

## Nền tảng

| Phần | Chọn | Vì sao, ngắn |
|---|---|---|
| JDK | **Java 25 LTS** (Temurin), bundle kèm app | Máy đang có 21; app ship JRE riêng |
| UI | **JavaFX 27 + AtlantaFX 3.0.0** | 27 có bản sửa D3D9 swap-chain làm Mica chạy trên painter nhanh, cộng các fix cho table lớn |
| ML | **weka-stable 3.8.7** từ Maven Central | Đúng bản các installer chính thức ship; `commons-compress` phải khai báo tay |
| Build | **Maven**, 3 module `core` → `app` → `dist` | `core` không có UI và là chỗ duy nhất `import weka.*` |
| Đóng gói | **jpackage app-image → Inno Setup 7** | Cài theo user, không cần quyền admin |
| Nền tảng đích | **Windows x64** | Mica qua `StageStyle.UNIFIED`; macOS/Linux là NON-goal |

## Cài môi trường

Đây là đúng cấu hình đã được xác nhận chạy được ở S00 trên máy build, kèm những cái bẫy đã mất
thời gian một lần rồi.

**1. Windows.** x64, build 26100 trở lên (Windows 11 24H2). Máy build báo ProductName là
`Windows 10 IoT Enterprise LTSC 2024` nhưng `CurrentBuildNumber` là 26100 — đừng tin cái tên,
tin cái số build.

**2. Temurin JDK 25.** Cài bản x64 từ [adoptium.net](https://adoptium.net/). Trên máy build nó
nằm ở `C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot`.

Chọn JDK **không cần quyền admin** nghĩa là đặt `JAVA_HOME` ở phạm vi User, không sửa `PATH`:

```bash
[Environment]::SetEnvironmentVariable('JAVA_HOME','C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot','User')
```

Lý do: Windows nối User `PATH` **sau** Machine `PATH`, nên một entry User không bao giờ che được
`jdk-21\bin` đã nằm ở phạm vi Machine. Maven thì đọc `JAVA_HOME`, nên mọi lần build vẫn chạy
trên 25. Mở shell mới rồi kiểm tra:

```bash
& "$env:JAVA_HOME\bin\java" -version
```

> **Bẫy.** `java -version` trơn trên máy này trả lời **21**, vì Machine `PATH` thắng. Bất cứ
> kiểm tra nào muốn chứng minh build thật sự chạy trên JDK 25 đều phải gọi
> `$env:JAVA_HOME\bin\java`, không thì đó là một lần fail giả, vĩnh viễn.

**3. Maven 3.10.0.** `winget` không hề có package `Apache.Maven`, nên dùng scoop:

```bash
scoop install maven
```

Nó nằm ở `~\scoop\apps\maven\current`, `bin` được thêm vào User `PATH`. scoop **không** tạo shim
`mvn` dưới `scoop\shims`. Kiểm tra `mvn -v` — dòng `Java version` phải là 25.

**4. Toạ độ dependency** (đã xác nhận resolve được hết):

- JavaFX GA trên nhánh 27 là chuỗi phiên bản `27` trơn — không tồn tại `27.0.1`.
- AtlantaFX 3.x chỉ có đúng `3.0.0`.
- `weka-stable` đánh dấu `commons-compress` là optional nên nó **không** được kế thừa transitive.
  Không khai báo tay thì việc đọc `.arff.gz` và `.arff.bz2` fail lúc runtime, không phải lúc build.

**5. Inno Setup 7** — chỉ cần đến S06, chưa cài trên máy build.

**Chạy thử spike:** `cd spike`, rồi dùng `mvnw` trong đó. `spike/` có pom riêng, **ngoài** reactor
Maven, và sống lâu hơn S00 có chủ ý — khi JavaFX 28 ra, chạy lại các probe trong đó và so với số
cũ thay vì tranh luận theo ký ức. Toàn bộ số đo và caveat nằm ở [`spike/README.md`](spike/README.md).

## Lộ trình

Mỗi stage có bài test thoát riêng; chi tiết và điều kiện done nằm ở
[`memory/STAGES.md`](memory/STAGES.md).

| Stage | Nội dung | Trạng thái |
|---|---|---|
| **S00** | Chứng minh nền tảng: Mica trên `StageStyle.UNIFIED` + painter nhanh, 100k dòng ở 60fps, 2.000 thuộc tính, và đo trần số cột thật | ✅ xong 2026-10-08 |
| **S01** | Vỏ ứng dụng: icon rail, document tabs, bottom panel, status bar, command palette, keymap xoá được binding mặc định — không một lệnh gọi Weka nào | 🔨 đang làm |
| **S02** | Bộ sửa tham số sinh từ `OptionHandler.listOptions()`, thay thế hộp thoại `GenericObjectEditor` | ⬜ |
| **S03** | Một lát dọc Preprocess + Classify end to end — chứng minh engine split, run model, cancel và history cùng lúc | ⬜ |
| **S04** | Cluster, Associate, Select attributes, Visualize — lặp lại, không phát minh | ⬜ |
| **S05** | Package manager trên `WekaPackageManager`: pin version, xem trước dependency, ba trạng thái cài/chưa load/có sẵn | ⬜ |
| **S06** | Ship: installer bundle JRE riêng, chạy trên Windows 10, không cần admin | ⬜ |

Những thứ **không** làm ở v1 được ghi rõ là NON-goal trong
[`memory/PLAN.md`](memory/PLAN.md) — KnowledgeFlow, Experimenter, nhúng panel Swing của package,
UI kết nối database, build macOS/Linux, và gói ngôn ngữ thứ hai. Mỗi dòng ở đó là một yêu cầu
mà session sau phải từ chối.

## Cấu trúc repo

```
.claude/     slash command + SessionStart hook của Claude Code
design/      mockup HTML — UI được chấm điểm trước khi build, không phải sau
memory/      bộ nhớ project: kế hoạch, quyết định, fact, trạng thái stage
spike/       mã S00 dùng một lần, pom riêng ngoài reactor Maven
CLAUDE.md    router: loại việc nào thì đọc file nào, và không đọc gì
```

`memory/` được git theo dõi **có chủ ý**, và công khai có chủ ý. Claude Code có quyền ghi tự động
vào mấy file trong đó, nên version control là thứ duy nhất làm một lần sửa sai — một fact bị
xoá, một `CURRENT.md` bị ghi đè — hiện ra thành diff và lùi lại được. Nếu bạn muốn hiểu các lựa
chọn trong code này, [`memory/DECISIONS.md`](memory/DECISIONS.md) ghi cả phương án bị loại và
lý do loại.

## Giấy phép

**GPL-3.0-or-later.** [`LICENSE`](LICENSE) là bản GPL-3.0 nguyên văn.

Weka: Copyright © University of Waikato, Hamilton, New Zealand — GPL-3.0-or-later, không có
linking exception. Waka link `weka-stable` trong cùng process, nên nó là derivative work và
GPL là bắt buộc, không phải lựa chọn. Việc này được chấp nhận một cách cố ý: đổi lại chúng ta
có object `Instances` / `Evaluation` / `Package` có kiểu, toàn bộ API package manager chạy
headless, và không phải parse stdout. Màn hình About trong app sẽ dẫn lại Weka upstream và mang
theo thông báo GPLv3 — đó là nghĩa vụ giấy phép, không phải đồ trang trí.

Không có file âm thanh nào được commit vào repo này.
