import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:lucide_icons/lucide_icons.dart';
import 'package:paobar/app/theme/app_colors.dart';
import 'package:paobar/core/di/injector.dart';
import 'package:paobar/core/widgets/primary_button.dart';
import 'package:paobar/core/widgets/selectable_error_text.dart';

/// 对齐 web 的 Crawler.vue：输入 yopu.co URL → 后端抓取 → 保存入库。
/// 注意 POST /api/crawler/crawl-and-save 走 JWT，401 会触发登录弹窗。
class CrawlerPage extends StatefulWidget {
  const CrawlerPage({super.key});

  @override
  State<CrawlerPage> createState() => _CrawlerPageState();
}

class _CrawlerPageState extends State<CrawlerPage> {
  final _urlCtrl = TextEditingController();
  bool _loading = false;
  String? _message;
  bool _ok = false;

  @override
  void dispose() {
    _urlCtrl.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    final url = _urlCtrl.text.trim();
    if (url.isEmpty) return;
    setState(() {
      _loading = true;
      _message = null;
    });
    try {
      final res = await sl<Dio>().post<Map<String, dynamic>>(
        '/api/crawler/crawl-and-save',
        data: {'url': url},
      );
      final data = res.data;
      final songId = data?['id']?.toString();
      setState(() {
        _ok = true;
        _message = '抓取成功${songId == null ? '' : '，已入库 (id=$songId)'}';
      });
    } catch (e) {
      setState(() {
        _ok = false;
        _message = '抓取失败：$e';
      });
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('从网页抓取'),
        leading: IconButton(
          icon: const Icon(LucideIcons.chevronLeft),
          onPressed: () => context.canPop() ? context.pop() : context.go('/index'),
        ),
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const Text(
                '粘贴 yopu.co 的谱子 URL，后端会解析并保存到谱库。',
                style: TextStyle(color: AppColors.textSecondary, fontSize: 13),
              ),
              const SizedBox(height: 16),
              TextField(
                controller: _urlCtrl,
                keyboardType: TextInputType.url,
                autocorrect: false,
                decoration: const InputDecoration(
                  hintText: 'https://yopu.co/view/xxxxx',
                  prefixIcon: Icon(LucideIcons.link, size: 18),
                ),
              ),
              const SizedBox(height: 16),
              PrimaryButton(
                label: '开始抓取',
                icon: LucideIcons.download,
                loading: _loading,
                onPressed: _submit,
              ),
              if (_message != null) ...[
                const SizedBox(height: 16),
                _ok
                    ? Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: const Color(0x1A22C55E),
                          border: Border.all(color: const Color(0x3322C55E)),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Text(
                          _message!,
                          style: const TextStyle(color: AppColors.success),
                        ),
                      )
                    : SelectableErrorText(_message!),
              ],
            ],
          ),
        ),
      ),
    );
  }
}
